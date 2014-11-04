package org.infinispan.server.test.query;

import org.infinispan.arquillian.core.InfinispanResource;
import org.infinispan.arquillian.core.RemoteInfinispanServer;
import org.infinispan.arquillian.core.RunningServer;
import org.infinispan.arquillian.core.WithRunningServer;
import org.jboss.arquillian.junit.Arquillian;
import org.junit.runner.RunWith;

/**
 * Tests for remote queries over HotRod on a local non-indexed cache.
 *
 * @author Adrian Nistor
 * @author Martin Gencur
 * @since 7.0
 */
@RunWith(Arquillian.class)
@WithRunningServer({@RunningServer(name = "remote-query")})
public class RemoteNonIndexedQueryIT extends RemoteQueryIT {

   @InfinispanResource("remote-query")
   protected RemoteInfinispanServer server;

   public RemoteNonIndexedQueryIT() {
      super("local", "notindexed");
   }

   @Override
   protected RemoteInfinispanServer getServer() {
      return server;
   }
}
