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
package org.apache.camel.main;

import org.apache.camel.spi.BootstrapCloseable;
import org.apache.camel.spi.Configurer;
import org.apache.camel.spi.Metadata;

/**
 * Authentication configuration for embedded HTTP server for standalone Camel applications (not Spring Boot / Quarkus).
 */
@Configurer(bootstrap = true)
public class HttpServerAuthenticationConfigurationProperties implements BootstrapCloseable {

    private HttpServerConfigurationProperties parent;

    @Metadata
    private boolean enabled;

    private HttpServerBasicAuthenticationProperties basic;

    public HttpServerAuthenticationConfigurationProperties(HttpServerConfigurationProperties parent) {
        this.parent = parent;
    }

    public HttpServerConfigurationProperties end() {
        return parent;
    }

    @Override
    public void close() {
        if (basic != null) {
            basic.close();
            basic = null;
        }
        parent = null;
    }

    public boolean isEnabled() {
        return enabled;
    }

    /**
     * Whether embedded HTTP server is enabled. By default, the server is not enabled.
     */
    public void setEnabled(boolean enabled) {
        this.enabled = enabled;
    }

    /**
     * Whether embedded HTTP server is enabled. By default, the server is not enabled.
     */
    public HttpServerAuthenticationConfigurationProperties withEnabled(boolean enabled) {
        this.enabled = enabled;
        return this;
    }

    /**
     * To configure embedded HTTP server authentication (for standalone applications; not Spring Boot or Quarkus)
     */
    public HttpServerBasicAuthenticationProperties basic() {
        if (basic == null) {
            basic = new HttpServerBasicAuthenticationProperties(this);
        }
        return basic;
    }

    public HttpServerBasicAuthenticationProperties getBasic() {
        return basic;
    }

    /**
     * To configure embedded HTTP server authentication (for standalone applications; not Spring Boot or Quarkus)
     */
    public void setBasic(
            HttpServerBasicAuthenticationProperties basic) {
        this.basic = basic;
    }

    /**
     * To configure embedded HTTP server authentication (for standalone applications; not Spring Boot or Quarkus)
     */
    public HttpServerAuthenticationConfigurationProperties withBasicAuthenticationProperties(
            HttpServerBasicAuthenticationProperties basicAuthenticationProperties) {
        this.basic = basicAuthenticationProperties;
        return this;
    }


}
