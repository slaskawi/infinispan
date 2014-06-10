package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#DENY} handler.
 *
 * @author Tristan Tarrant
 * @since 6.1
 */
public class DenyCommandHandler extends RoleManipulationCommandHandler {

    public DenyCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.DENY, buffer);
    }

}
