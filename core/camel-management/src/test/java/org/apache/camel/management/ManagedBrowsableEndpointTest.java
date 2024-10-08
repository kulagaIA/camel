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
package org.apache.camel.management;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.apache.camel.management.DefaultManagementObjectNameStrategy.TYPE_ENDPOINT;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;

@DisabledOnOs(OS.AIX)
public class ManagedBrowsableEndpointTest extends ManagementTestSupport {

    @Test
    public void testBrowseableEndpoint() throws Exception {
        getMockEndpoint("mock:result").expectedMessageCount(2);

        template.sendBody("direct:start", "Hello World");
        template.sendBody("direct:start", "Bye World");

        assertMockEndpointsSatisfied();

        MBeanServer mbeanServer = getMBeanServer();

        ObjectName name = getCamelObjectName(TYPE_ENDPOINT, "mock://result");
        String uri = (String) mbeanServer.getAttribute(name, "EndpointUri");
        assertEquals("mock://result", uri);

        Integer size = (Integer) mbeanServer.invoke(name, "queueSize", null, null);
        assertEquals(2, size.intValue());

        String out
                = (String) mbeanServer.invoke(name, "browseExchange", new Object[] { 0 }, new String[] { "java.lang.Integer" });
        assertNotNull(out);
        // message body is not dumped when browsing exchange
        assertFalse(out.contains("Hello World"));

        out = (String) mbeanServer.invoke(name, "browseExchange", new Object[] { 1 }, new String[] { "java.lang.Integer" });
        assertNotNull(out);
        // message body is not dumped when browsing exchange
        assertFalse(out.contains("Bye World"));

        out = (String) mbeanServer.invoke(name, "browseMessageBody", new Object[] { 1 }, new String[] { "java.lang.Integer" });
        assertNotNull(out);
        assertEquals("Bye World", out);
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").to("log:foo").to("mock:result");
            }
        };
    }

}
