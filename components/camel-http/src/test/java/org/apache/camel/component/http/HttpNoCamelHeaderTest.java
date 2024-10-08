/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.apache.camel.component.http;

import org.apache.camel.Exchange;
import org.apache.hc.core5.http.Header;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.fail;

public class HttpNoCamelHeaderTest extends BaseHttpTest {

    private HttpServer localServer;

    @Override
    public void setupResources() throws Exception {
        localServer = ServerBootstrap.bootstrap()
                .setCanonicalHostName("localhost").setHttpProcessor(getBasicHttpProcessor())
                .setConnectionReuseStrategy(getConnectionReuseStrategy()).setResponseFactory(getHttpResponseFactory())
                .setSslContext(getSSLContext())
                .register("/hello", (request, response, context) -> {
                    response.setCode(HttpStatus.SC_OK);
                    Object header = request.getFirstHeader(Exchange.FILE_NAME);
                    assertNull(header, "There should be no Camel header");

                    for (Header h : request.getHeaders()) {
                        if (h.getName().startsWith("Camel") || h.getName().startsWith("org.apache.camel")) {
                            fail("There should be no Camel header");
                        }
                    }

                    // set ar regular and Camel header
                    response.setHeader("MyApp", "dude");
                    response.setHeader(Exchange.TO_ENDPOINT, "foo");
                }).create();
        localServer.start();
    }

    @Override
    public void cleanupResources() throws Exception {

        if (localServer != null) {
            localServer.stop();
        }
    }

    @Test
    public void testNoCamelHeader() {
        Exchange out = template.request(
                "http://localhost:" + localServer.getLocalPort() + "/hello",
                exchange -> {
                    exchange.getIn().setHeader(Exchange.CONTENT_TYPE, "text/plain");
                    exchange.getIn().setHeader(Exchange.FILE_NAME, "hello.txt");
                    exchange.getIn().setBody("This is content");
                });

        assertNotNull(out);
        assertFalse(out.isFailed(), "Should not fail");
        assertEquals("dude", out.getMessage().getHeader("MyApp"));
        assertNull(out.getMessage().getHeader(Exchange.TO_ENDPOINT));
    }
}
