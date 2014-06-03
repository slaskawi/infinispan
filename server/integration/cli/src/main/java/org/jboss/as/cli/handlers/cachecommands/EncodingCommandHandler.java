package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#ENCODING} handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class EncodingCommandHandler extends NoArgumentsCliCommandHandler {

   public EncodingCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.ENCODING, buffer);
      new ArgumentWithValue(this, null, 0, "--codec");
      new ArgumentWithoutValue(this, "--list");
   }
}
