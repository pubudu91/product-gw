package org.wso2.carbon.gateway.httpcompliance.tests.successful;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClient;
import org.wso2.carbon.gateway.test.clients.GatewayAdminClientImpl;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import java.io.File;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class HTTP202ComplianceTest {
    private GatewayAdminClient gwClient;
    private HttpServerOperationBuilderContext emulator;
    private static final String HOST = "127.0.0.1";
    private final int PORT = 9090;
    private static final String RESPONSE_BODY = "Request accepted and will be processed in due time";
    private File clientRequestPayload;

    @BeforeClass
    public void setup() throws Exception {
        clientRequestPayload = new File(getClass().getClassLoader()
                .getResource("test-payloads/client-request-payload.txt").toURI());
        gwClient = new GatewayAdminClientImpl();
        gwClient.startGateway();
        gwClient.deployArtifact("artifacts/http-compliance-test-camel-context.xml");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withPath("/user1")
                        .withMethod(HttpMethod.POST)
                        .withBody(clientRequestPayload))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .when(request()
                        .withPath("/user1")
                        .withMethod(HttpMethod.POST)
                        .withBody(clientRequestPayload)
                        .withBody("Body included"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.ACCEPTED)
                        .withBody(RESPONSE_BODY))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwClient.stopGateway();
        emulator.stop();
        gwClient.cleanArtifacts();
    }

    @Test
    public void test202POSTRequestWithPayloadWithoutBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(PORT))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/new-route")
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r1")
                        .withBody(clientRequestPayload))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }

    @Test
    public void test202POSTRequestWithPayloadWithBody() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(HOST).port(PORT))

                .when(HttpClientRequestBuilderContext.request()
                        .withPath("/new-route")
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r1")
                        .withBody(clientRequestPayload)
                        .withBody("Body included"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.ACCEPTED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), RESPONSE_BODY);
    }
}