package org.infinispan.server.router.integration;

import static io.netty.buffer.Unpooled.wrappedBuffer;
import static io.netty.handler.codec.http.HttpMethod.POST;
import static io.netty.handler.codec.http.HttpVersion.HTTP_1_1;

import java.net.InetAddress;
import java.util.Queue;

import org.infinispan.rest.RestServer;
import org.infinispan.rest.http2.NettyHttpClient;
import org.infinispan.server.router.Router;
import org.infinispan.server.router.configuration.builder.RouterConfigurationBuilder;
import org.infinispan.server.router.router.EndpointRouter;
import org.infinispan.server.router.routes.Route;
import org.infinispan.server.router.routes.rest.NettyRestServerRouteDestination;
import org.infinispan.server.router.routes.singleport.SinglePortRouteSource;
import org.infinispan.server.router.utils.RestTestingUtil;
import org.junit.Test;

import io.netty.handler.codec.http.DefaultFullHttpRequest;
import io.netty.handler.codec.http.FullHttpRequest;
import io.netty.handler.codec.http.FullHttpResponse;
import io.netty.handler.ssl.OpenSsl;
import io.netty.util.CharsetUtil;

public class SinglePortTest {

    public static final String KEY_STORE_PATH = SinglePortTest.class.getClassLoader().getResource("./default_server_keystore.jks").getPath();
    public static final String KEY_STORE_PASSWORD = "secret";
    public static final String TRUST_STORE_PATH = SinglePortTest.class.getClassLoader().getResource("./default_client_truststore.jks").getPath();
    public static final String TRUST_STORE_PASSWORD = "secret";

    @Test
    public void shouldUpgradeThroughHTTP11UpgradeHeaders() throws Exception {
        //given
        RestServer restServer1 = RestTestingUtil.createDefaultRestServer("default");

        NettyRestServerRouteDestination restDestination = new NettyRestServerRouteDestination("rest1", restServer1);
        SinglePortRouteSource singlePortSource = new SinglePortRouteSource();
        Route<SinglePortRouteSource, NettyRestServerRouteDestination> routeToRest = new Route<>(singlePortSource, restDestination);

        RouterConfigurationBuilder routerConfigurationBuilder = new RouterConfigurationBuilder();
        routerConfigurationBuilder
                .singlePort()
                    .port(8080)
                    .ip(InetAddress.getLoopbackAddress())
                .routing()
                    .add(routeToRest);

        Router router = new Router(routerConfigurationBuilder.build());
        router.start();
        int port = router.getRouter(EndpointRouter.Protocol.SINGLE_PORT).get().getPort();

        //when
        NettyHttpClient client = NettyHttpClient.newHttp2ClientWithHttp11Upgrade();
        client.start("localhost", port);

        FullHttpRequest putValueInCacheRequest = new DefaultFullHttpRequest(HTTP_1_1, POST, "/rest/default/test",
              wrappedBuffer("test".getBytes(CharsetUtil.UTF_8)));

        //when
        client.sendRequest(putValueInCacheRequest);
        Queue<FullHttpResponse> responses = client.getResponses();

        System.out.println(responses.element());

        router.stop();
    }

    @Test
    public void shouldUpgradeThroughALPN() throws Exception {
        if (!OpenSsl.isAlpnSupported()) {
            throw new IllegalStateException("OpenSSL is not present, can not test TLS/ALPN support. Version: " + OpenSsl.versionString() + " Cause: " + OpenSsl.unavailabilityCause());
        }

        //given
        RestServer restServer1 = RestTestingUtil.createDefaultRestServer("default");

        NettyRestServerRouteDestination restDestination = new NettyRestServerRouteDestination("rest1", restServer1);
        SinglePortRouteSource singlePortSource = new SinglePortRouteSource();
        Route<SinglePortRouteSource, NettyRestServerRouteDestination> routeToRest = new Route<>(singlePortSource, restDestination);

        RouterConfigurationBuilder routerConfigurationBuilder = new RouterConfigurationBuilder();
        routerConfigurationBuilder
              .singlePort()
              .sslWithAlpn(KEY_STORE_PATH, KEY_STORE_PASSWORD.toCharArray())
              .port(8080)
              .ip(InetAddress.getLoopbackAddress())
              .routing()
              .add(routeToRest);

        Router router = new Router(routerConfigurationBuilder.build());
        router.start();
        int port = router.getRouter(EndpointRouter.Protocol.SINGLE_PORT).get().getPort();

        //when
        NettyHttpClient client = NettyHttpClient.newHttp2ClientWithALPN(TRUST_STORE_PATH, TRUST_STORE_PASSWORD);
        client.start("localhost", port);

        FullHttpRequest putValueInCacheRequest = new DefaultFullHttpRequest(HTTP_1_1, POST, "/rest/default/test",
              wrappedBuffer("test".getBytes(CharsetUtil.UTF_8)));

        //when
        client.sendRequest(putValueInCacheRequest);
        Queue<FullHttpResponse> responses = client.getResponses();

        System.out.println(responses.element());

        router.stop();
    }
}
