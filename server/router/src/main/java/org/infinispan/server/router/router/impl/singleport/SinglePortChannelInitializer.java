package org.infinispan.server.router.router.impl.singleport;

import org.infinispan.rest.Http11RequestHandler;
import org.infinispan.rest.Http11To2UpgradeHandler;
import org.infinispan.rest.Http20RequestHandler;
import org.infinispan.rest.RestServer;
import org.infinispan.server.core.transport.NettyChannelInitializer;
import org.infinispan.server.core.transport.NettyTransport;

import io.netty.channel.Channel;
import io.netty.handler.ssl.ApplicationProtocolConfig;
import io.netty.handler.ssl.ApplicationProtocolNames;

/**
 * Netty pipeline initializer for Single Port
 *
 * @author Sebastian Łaskawiec
 */
class SinglePortChannelInitializer extends NettyChannelInitializer {

   private final Http11To2UpgradeHandler http11To2UpgradeHandler;

   public SinglePortChannelInitializer(SinglePortEndpointRouter server, RestServer targetRestServer, NettyTransport transport) {
      super(server, transport, null, null);
      http11To2UpgradeHandler = new Http11To2UpgradeHandler(new Http11RequestHandler(targetRestServer), new Http20RequestHandler(targetRestServer), targetRestServer.getConfiguration().maxContentLength(), server.getConfiguration().ssl().enabled());
   }

   @Override
   public void initializeChannel(Channel ch) throws Exception {
      super.initializeChannel(ch);
      if (server.getConfiguration().ssl().enabled()) {
         ch.pipeline().addLast(http11To2UpgradeHandler);
      } else {
         http11To2UpgradeHandler.configurePipeline(ch.pipeline(), ApplicationProtocolNames.HTTP_1_1);
      }
   }

   @Override
   protected ApplicationProtocolConfig getAlpnConfiguration() {
      return http11To2UpgradeHandler.getAlpnConfiguration();
   }

}
