package org.infinispan.server.router.configuration.builder;

import java.io.File;
import java.util.HashMap;
import java.util.Map;

import org.infinispan.server.core.configuration.SslConfiguration;
import org.infinispan.server.core.configuration.SslEngineConfiguration;
import org.infinispan.server.router.configuration.HotRodRouterConfiguration;
import org.infinispan.server.router.configuration.SinglePortRouterConfiguration;

/**
 * Configuration builder for Single Port.
 *
 * @author Sebastian Łaskawiec
 */
public class SinglePortRouterBuilder extends AbstractRouterBuilder {

    private int sendBufferSize = 0;
    private int receiveBufferSize = 0;
    private String name = "single-port";
    private String keystorePath;
    private char[] keystorePassword;

    /**
     * Creates new {@link SinglePortRouterBuilder}.
     *
     * @param parent Parent {@link ConfigurationBuilderParent}
     */
    public SinglePortRouterBuilder(ConfigurationBuilderParent parent) {
        super(parent);
    }

    /**
     * Builds {@link HotRodRouterConfiguration}.
     */
    public SinglePortRouterConfiguration build() {
        if (this.enabled) {
            try {
                validate();
            } catch (Exception e) {
                throw logger.configurationValidationError(e);
            }
            SslConfiguration sslConfiguration = null;
            if (keystorePath != null) {
                SslEngineConfiguration engineConfiguration = new SslEngineConfiguration(keystorePath, null, keystorePassword, null, null, null, null, null, null, null);
                Map<String, SslEngineConfiguration> sniMapping = new HashMap<>();
                sniMapping.put("*", engineConfiguration);
                sslConfiguration = new SslConfiguration(true, false, sniMapping);
            }
            return new SinglePortRouterConfiguration(name, ip, port, sendBufferSize, receiveBufferSize, sslConfiguration);
        }
        return null;
    }

    /**
     * Sets Send buffer size
     *
     * @param sendBufferSize Send buffer size, must be greater than 0.
     */
    public SinglePortRouterBuilder sendBufferSize(int sendBufferSize) {
        this.sendBufferSize = sendBufferSize;
        return this;
    }

    /**
     * Sets Receive buffer size.
     *
     * @param receiveBufferSize Receive buffer size, must be greater than 0.
     */
    public SinglePortRouterBuilder receiveBufferSize(int receiveBufferSize) {
        this.receiveBufferSize = receiveBufferSize;
        return this;
    }

    /**
     * Sets this server name.
     *
     * @param name The name of the server.
     */
    public SinglePortRouterBuilder name(String name) {
        this.name = name;
        return this;
    }

    public SinglePortRouterBuilder sslWithAlpn(String keystorePath, char[] keystorePassword) {
        this.keystorePassword = keystorePassword;
        this.keystorePath = keystorePath;
        return this;
    }

    @Override
    protected void validate() {
        super.validate();
        if (receiveBufferSize < 0) {
            throw new IllegalArgumentException("Receive buffer size can not be negative");
        }
        if (sendBufferSize < 0) {
            throw new IllegalArgumentException("Send buffer size can not be negative");
        }
        if (keystorePath != null && !new File(keystorePath).exists()) {
            throw new IllegalArgumentException("Keystore path does not exist");
        }
    }
}
