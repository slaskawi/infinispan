package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#GRANT} handler.
 *
 * @author Tristan Tarrant
 * @since 6.1
 */
public class GrantCommandHandler extends RoleManipulationCommandHandler {

    public GrantCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.GRANT, buffer);
    }

}

