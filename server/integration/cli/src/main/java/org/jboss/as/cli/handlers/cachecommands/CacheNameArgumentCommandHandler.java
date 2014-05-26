package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.CommandHandler} implementation for Infinispan CLI commands which have the cache name as
 * an argument.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class CacheNameArgumentCommandHandler extends NoArgumentsCliCommandHandler {

   protected final ArgumentWithValue cacheName;

   public CacheNameArgumentCommandHandler(CacheCommand command, CliCommandBuffer buffer) {
      super(command, buffer);
      cacheName = new ArgumentWithValue(this, new CacheNameCommandCompleter(), 0, "--cache-name");
   }
}
