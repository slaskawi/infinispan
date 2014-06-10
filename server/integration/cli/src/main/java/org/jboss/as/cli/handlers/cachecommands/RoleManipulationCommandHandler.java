package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#DENY} handler.
 *
 * @author Tristan Tarrant
 * @since 6.1
 */
public abstract class RoleManipulationCommandHandler extends NoArgumentsCliCommandHandler {

    public RoleManipulationCommandHandler(CacheCommand command, CliCommandBuffer buffer) {
      super(command, buffer);
      new ArgumentWithValue(this, null, 0, "--role");
      new ArgumentWithoutValue(this, 1, "--to");
      new ArgumentWithValue(this, null, 2, "--principal");
    }

}
