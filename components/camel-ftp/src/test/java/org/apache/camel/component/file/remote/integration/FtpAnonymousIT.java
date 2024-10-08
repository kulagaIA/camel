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
package org.apache.camel.component.file.remote.integration;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

/**
 * Unit test that ftp consumer for anonymous login
 */
public class FtpAnonymousIT extends FtpServerTestSupport {

    private String getFtpUrl(String user, String password) {
        StringBuilder url = new StringBuilder("ftp://");
        url.append(user == null ? "" : user + "@");
        url.append("localhost:{{ftp.server.port}}/");
        url.append(password == null ? "" : "?password=" + password);
        return url.toString();
    }

    @Override
    public void doPostSetup() throws Exception {
        prepareFtpServer();
    }

    @Test
    public void testAnonymous() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);

        mock.assertIsSatisfied();
    }

    private void prepareFtpServer() {
        sendFile(getFtpUrl("admin", "admin"), "Hello World", "hello.xml");
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            public void configure() {
                from(getFtpUrl(null, null)).to("mock:result");
            }
        };
    }
}
