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

import java.util.Locale;
import java.util.concurrent.TimeUnit;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.apache.camel.builder.RouteBuilder;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.DisabledOnOs;
import org.junit.jupiter.api.condition.OS;

import static org.junit.jupiter.api.Assertions.assertEquals;

@DisabledOnOs(OS.AIX)
public class ManagedShutdownStrategyTest extends ManagementTestSupport {

    @Test
    public void testManagedShutdownStrategy() throws Exception {
        // set timeout to 300
        context.getShutdownStrategy().setTimeout(300);

        MBeanServer mbeanServer = getMBeanServer();

        ObjectName on = getContextObjectName();

        Long timeout = (Long) mbeanServer.getAttribute(on, "Timeout");
        assertEquals(300, timeout.longValue());

        TimeUnit unit = (TimeUnit) mbeanServer.getAttribute(on, "TimeUnit");
        assertEquals("seconds", unit.toString().toLowerCase(Locale.ENGLISH));
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:foo").to("mock:foo");
            }
        };
    }
}
