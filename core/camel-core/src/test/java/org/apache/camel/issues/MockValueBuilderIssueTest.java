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
package org.apache.camel.issues;

import org.apache.camel.ContextTestSupport;
import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.component.mock.MockEndpoint;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class MockValueBuilderIssueTest extends ContextTestSupport {

    @Test
    public void testMockValueBuilder() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).exchangeProperty("foo").convertTo(String.class).contains("2");

        template.sendBody("direct:start", "Hello World");

        assertMockEndpointsSatisfied();
    }

    @Test
    public void testMockValueBuilderFail() {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).exchangeProperty("foo").convertTo(String.class).contains("4");

        template.sendBody("direct:start", "Hello World");

        Throwable e = assertThrows(Throwable.class, this::assertMockEndpointsSatisfied,
                "Should fail");

        String s = "Assertion error at index 0 on mock mock://result with predicate: exchangeProperty(foo) contains 4 evaluated as: 123 contains 4";
        assertTrue(e.getMessage().startsWith(s));
    }

    @Test
    public void testMockValueBuilderNotSatisfied() throws Exception {
        MockEndpoint mock = getMockEndpoint("mock:result");
        mock.expectedMessageCount(1);
        mock.message(0).exchangeProperty("foo").convertTo(String.class).contains("4");

        template.sendBody("direct:start", "Hello World");

        mock.assertIsNotSatisfied();
    }

    @Override
    protected RouteBuilder createRouteBuilder() {
        return new RouteBuilder() {
            @Override
            public void configure() {
                from("direct:start").setProperty("foo", constant(123)).to("mock:result");
            }
        };
    }
}
