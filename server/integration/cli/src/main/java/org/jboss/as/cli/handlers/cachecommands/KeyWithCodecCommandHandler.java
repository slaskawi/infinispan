package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.CommandHandler} implementation which have a key and the code as arguments.
 *
 * @author Pedro Ruivo
 * @since 6.0
 */
public class KeyWithCodecCommandHandler extends KeyCommandHandler {

   protected final ArgumentWithValue codec;

   public KeyWithCodecCommandHandler(CacheCommand cacheCommand, CliCommandBuffer buffer) {
      super(cacheCommand, buffer);
      this.codec = new ArgumentWithValue(this, null, -1, "--codec");
   }

}
