/*
 * Copyright (c) 2016, WSO2 Inc. (http://www.wso2.org) All Rights Reserved.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.wso2.carbon.gateway.httpcompliance.tests.responses.clienterror;

import io.netty.handler.codec.http.HttpMethod;
import io.netty.handler.codec.http.HttpResponseStatus;
import org.testng.Assert;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;
import org.wso2.carbon.gateway.httpcompliance.tests.internal.GWIntegrationTest;
import org.wso2.gw.emulator.dsl.Emulator;
import org.wso2.gw.emulator.http.client.contexts.HttpClientConfigBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientRequestBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseBuilderContext;
import org.wso2.gw.emulator.http.client.contexts.HttpClientResponseProcessorContext;
import org.wso2.gw.emulator.http.params.Header;
import org.wso2.gw.emulator.http.server.contexts.HttpServerOperationBuilderContext;

import java.io.File;

import static org.wso2.gw.emulator.http.server.contexts.HttpServerConfigBuilderContext.configure;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerRequestBuilderContext.request;
import static org.wso2.gw.emulator.http.server.contexts.HttpServerResponseBuilderContext.response;

public class HTTP412ComplianceTest extends GWIntegrationTest {
    private HttpServerOperationBuilderContext emulator;
    private String host = "127.0.0.1";
    private int port = 9090;
    private String serverResponse = "412 - Precondition Failed";
    private Header condition = new Header("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT");

    @BeforeClass
    public void setup() throws Exception {
        gwHotDeployArtifacts("artifacts" + File.separator + "http-compliance-test-camel-context.xml",
                "/new-route");
        emulator = startHttpEmulator();
        Thread.sleep(1000);
    }

    private HttpServerOperationBuilderContext startHttpEmulator() {
        return Emulator.getHttpEmulator().server().given(configure().host("127.0.0.1").port(6065).context("/users"))

                .when(request()
                        .withMethod(HttpMethod.GET)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.HEAD)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withPath("/user3")
                        .withBody("name=WSO2&location=Colombo10"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withPath("/user2"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withHeader("Content-Type", "application/json")
                        .withPath("/user1"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED)
                        .withBody(serverResponse))

                .when(request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("If-Unmodified-Since", "Mon, 15 Feb 2016 19:43:31 GMT")
                        .withPath("/user2")
                        .withBody("Resource to be created at the given URI"))
                .then(response()
                        .withStatusCode(HttpResponseStatus.PRECONDITION_FAILED)
                        .withBody(serverResponse))

                .operation().start();
    }

    @AfterClass(alwaysRun = true)
    public void cleanup() throws Exception {
        gwCleanup();
        emulator.stop();
    }

    @Test
    public void test412GETRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.GET)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1")
                        .withHeaders(condition))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse);
    }

    @Test
    public void test412HEADRequest() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.HEAD)
                        .withPath("/new-route")
                        .withHeader("routeId", "r1")
                        .withHeaders(condition))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertNull(response.getReceivedResponseContext().getResponseBody());
    }

    @Test
    public void test412POSTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r3")
                        .withHeaders(condition)
                        .withPath("/new-route")
                        .withBody("name=WSO2&location=Colombo10"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test412POSTRequestWithoutPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r2")
                        .withHeaders(condition)
                        .withPath("/new-route"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test412POSTRequestWithoutPayloadWithContentType() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.POST)
                        .withHeader("routeId", "r1")
                        .withHeaders(condition, new Header("Content-Type", "application/json"))
                        .withPath("/new-route"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }

    @Test
    public void test412PUTRequestWithPayload() throws Exception {
        HttpClientResponseProcessorContext response = Emulator.getHttpEmulator().client()
                .given(HttpClientConfigBuilderContext.configure().host(host).port(port))

                .when(HttpClientRequestBuilderContext.request()
                        .withMethod(HttpMethod.PUT)
                        .withHeader("routeId", "r2")
                        .withHeaders(condition)
                        .withPath("/new-route")
                        .withBody("Resource to be created at the given URI"))

                .then(HttpClientResponseBuilderContext.response().assertionIgnore()).operation().send();

        Assert.assertEquals(response.getReceivedResponse().getStatus(), HttpResponseStatus.PRECONDITION_FAILED,
                "Expected response code not found");

        Assert.assertEquals(response.getReceivedResponseContext().getResponseBody(), serverResponse,
                "Response body does not match the expected response body");
    }
}