package org.apache.camel.main;

import org.apache.camel.spi.BootstrapCloseable;
import org.apache.camel.spi.Configurer;

@Configurer(bootstrap = true)
public class HttpServerBasicAuthenticationProperties implements BootstrapCloseable {

    private HttpServerAuthenticationConfigurationProperties parent;

    public HttpServerBasicAuthenticationProperties(HttpServerAuthenticationConfigurationProperties parent) {
        this.parent = parent;
    }

    @Override
    public void close() {
        parent = null;
    }
}
