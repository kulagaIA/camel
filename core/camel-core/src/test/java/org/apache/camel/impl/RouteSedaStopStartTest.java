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
package org.apache.camel.impl;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.Route;
import org.apache.camel.StatefulService;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;

public class RouteSedaStopStartTest extends ContextTestSupport {

    @Test
    public void testStopStart() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedBodiesReceived("A");

        template.sendBody("seda:foo", "A");

        assertMockEndpointsSatisfied();

        log.info("Stopping");

        // now suspend and dont expect a message to be routed
        resetMocks();
        mock.expectedMessageCount(0);
        context.getRouteController().stopRoute("foo");

        assertEquals("Stopped", context.getRouteController().getRouteStatus("foo").name());
        Route route = context.getRoute("foo");
        if (route instanceof StatefulService statefulService) {
            assertEquals("Stopped", statefulService.getStatus().name());
        }

        template.sendBody("seda:foo", "B");
        mock.assertIsSatisfied(1000);

        log.info("Starting");

        // now resume and expect the previous message to be routed
        resetMocks();
        mock.expectedBodiesReceived("B");
        context.getRouteController().startRoute("foo");
        assertMockEndpointsSatisfied();

        assertEquals("Started", context.getRouteController().getRouteStatus("foo").name());
        route = context.getRoute("foo");
        if (route instanceof StatefulService statefulService) {
            assertEquals("Started", statefulService.getStatus().name());
        }
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("seda:foo").routeId("foo").to("log:foo").to("mock:result");
            }
        };
    }
}
