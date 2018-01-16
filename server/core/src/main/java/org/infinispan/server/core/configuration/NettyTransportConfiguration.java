package org.infinispan.server.core.configuration;

/**
 * Configuration for Netty Transport
 *
 * @author Sebastian Łaskawiec
 */
public interface NettyTransportConfiguration {
   boolean tcpNoDelay();

   int sendBufSize();

   int recvBufSize();

   int workerThreads();

   int idleTimeout();
}
