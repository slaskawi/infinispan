package org.infinispan.server.router.router.impl.singleport;

import java.lang.invoke.MethodHandles;
import java.net.InetAddress;
import java.net.InetSocketAddress;
import java.net.UnknownHostException;

import org.infinispan.commons.logging.LogFactory;
import org.infinispan.rest.RestChannelInitializer;
import org.infinispan.server.core.AbstractProtocolServer;
import org.infinispan.server.core.transport.NettyInitializers;
import org.infinispan.server.core.transport.NettyTransport;
import org.infinispan.server.router.RoutingTable;
import org.infinispan.server.router.configuration.SinglePortRouterConfiguration;
import org.infinispan.server.router.logging.RouterLogger;
import org.infinispan.server.router.router.EndpointRouter;
import org.infinispan.server.router.routes.Route;
import org.infinispan.server.router.routes.rest.NettyRestServerRouteDestination;
import org.infinispan.server.router.routes.singleport.SinglePortRouteSource;

import io.netty.channel.Channel;
import io.netty.channel.ChannelInboundHandler;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelOutboundHandler;

public class SinglePortEndpointRouter extends AbstractProtocolServer<SinglePortRouterConfiguration> implements EndpointRouter {

   private static final RouterLogger logger = LogFactory.getLog(MethodHandles.lookup().lookupClass(), RouterLogger.class);

   private RoutingTable routingTable;

   public SinglePortEndpointRouter(SinglePortRouterConfiguration configuration) {
      super(Protocol.SINGLE_PORT.toString());
      this.configuration = configuration;
   }

   @Override
   public void start(RoutingTable routingTable) {
      this.routingTable = routingTable;
      InetSocketAddress address = new InetSocketAddress(configuration.host(), configuration.port());
      transport = new NettyTransport(address, configuration, getQualifiedName(), cacheManager);
      transport.initializeHandler(getInitializer());
      transport.start();
      logger.restRouterStarted(getTransport().getHostName() + ":" + getTransport().getPort());
   }

   @Override
   public void stop() {
     super.stop();
   }

   @Override
   public InetAddress getIp() {
      try {
         return InetAddress.getByName(getHost());
      } catch (UnknownHostException e) {
         throw new IllegalStateException("Unknown host", e);
      }
   }

   @Override
   public ChannelOutboundHandler getEncoder() {
      return null;
   }

   @Override
   public ChannelInboundHandler getDecoder() {
      return null;
   }

   @Override
   public ChannelInitializer<Channel> getInitializer() {
      Route<SinglePortRouteSource, NettyRestServerRouteDestination> route = routingTable.streamRoutes(SinglePortRouteSource.class, NettyRestServerRouteDestination.class)
            .findFirst().orElseThrow(() -> new IllegalStateException("There must be a REST route!"));

      SinglePortChannelInitializer restChannelInitializer = new SinglePortChannelInitializer(this, route.getRouteDesitnation().getRestServer(), transport);
      return new NettyInitializers(restChannelInitializer);
   }

   @Override
   public Protocol getProtocol() {
      return Protocol.SINGLE_PORT;
   }

}
