package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.CommandHandler} implementation which have a key as an argument.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class KeyCommandHandler extends NoArgumentsCliCommandHandler {

   protected final ArgumentWithValue key;

   public KeyCommandHandler(CacheCommand cacheCommand, CliCommandBuffer buffer) {
      this(cacheCommand, buffer, 0);
   }

   public KeyCommandHandler(CacheCommand cacheCommand, CliCommandBuffer buffer, int keyIndex) {
      super(cacheCommand, buffer);
      key = new ArgumentWithValue(this, null, keyIndex, "--key");
   }
}
