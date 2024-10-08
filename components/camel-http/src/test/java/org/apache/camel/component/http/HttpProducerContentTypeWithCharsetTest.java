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

import java.nio.charset.StandardCharsets;

import org.apache.camel.Exchange;
import org.apache.hc.core5.http.HttpStatus;
import org.apache.hc.core5.http.impl.bootstrap.HttpServer;
import org.apache.hc.core5.http.impl.bootstrap.ServerBootstrap;
import org.apache.hc.core5.http.io.entity.StringEntity;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class HttpProducerContentTypeWithCharsetTest extends BaseHttpTest {

    private static final String CONTENT_TYPE_WITHOUT_CHARSET
            = "multipart/form-data;boundary=---------------------------j2radvtrk";
    private static final String CONTENT_TYPE_WITH_CHARSET = CONTENT_TYPE_WITHOUT_CHARSET + ";charset=utf-8";

    private HttpServer localServer;

    private String endpointUrl;

    @Override
    public void setupResources() throws Exception {
        localServer = ServerBootstrap.bootstrap()
                .setCanonicalHostName("localhost").setHttpProcessor(getBasicHttpProcessor())
                .setConnectionReuseStrategy(getConnectionReuseStrategy()).setResponseFactory(getHttpResponseFactory())
                .setSslContext(getSSLContext())
                .register("/content", (request, response, context) -> {
                    String contentType = request.getFirstHeader(Exchange.CONTENT_TYPE).getValue();

                    assertEquals(CONTENT_TYPE_WITH_CHARSET.replace(";", "; "), contentType);
                    assertFalse(request.containsHeader(Exchange.CONTENT_ENCODING));

                    response.setEntity(new StringEntity(contentType, StandardCharsets.US_ASCII));
                    response.setCode(HttpStatus.SC_OK);
                }).create();
        localServer.start();

        endpointUrl = "http://localhost:" + localServer.getLocalPort();
    }

    @Override
    public void cleanupResources() throws Exception {

        if (localServer != null) {
            localServer.stop();
        }
    }

    @Test
    void testContentTypeWithCharsetInCharsetHeader() {
        Exchange out = template.request(endpointUrl + "/content", exchange -> {
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE_WITHOUT_CHARSET);
            exchange.getIn().setHeader(Exchange.CHARSET_NAME, "utf-8");
            exchange.getIn().setBody("This is content");
        });

        assertNotNull(out);
        assertFalse(out.isFailed(), "Should not fail");
        assertEquals(CONTENT_TYPE_WITH_CHARSET.replace(";", "; "), out.getMessage().getBody(String.class));
    }

    @Test
    void testContentTypeWithCharsetInContentTypeHeader() {
        Exchange out = template.request(endpointUrl + "/content", exchange -> {
            exchange.getIn().setHeader(Exchange.CONTENT_TYPE, CONTENT_TYPE_WITH_CHARSET);
            exchange.getIn().setBody("This is content");
        });

        assertNotNull(out);
        assertFalse(out.isFailed(), "Should not fail");
        assertEquals(CONTENT_TYPE_WITH_CHARSET.replace(";", "; "), out.getMessage().getBody(String.class));
    }

}
