package org.infinispan.server.router.configuration;

import java.net.InetAddress;

/**
 * {@link org.infinispan.server.router.MultiTenantRouter}'s configuration for REST.
 */
public class RestRouterConfiguration extends AbstractRouterConfiguration {

    /**
     * Creates new configuration based on the IP address and port.
     *
     * @param ip   The IP address used for binding. Can not be <code>null</code>.
     * @param port Port used for binding. Can be 0, in that case a random port is assigned.
     */
    public RestRouterConfiguration(InetAddress ip, int port) {
        super(ip, port);
    }
}
