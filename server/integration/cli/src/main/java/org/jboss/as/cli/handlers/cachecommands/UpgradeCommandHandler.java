package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandArgument;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.cli.util.CliCommandBuffer;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#UPGRADE} handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class UpgradeCommandHandler extends CacheNameArgumentCommandHandler {

   private final ArgumentWithoutValue all;
   private final ArgumentWithoutValue dumpKeys;
   private final ArgumentWithValue synchronize;
   private final ArgumentWithValue disconnect;

   public UpgradeCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.UPGRADE, buffer);
      all = new ArgumentWithoutValue(this, -1, "--all");
      dumpKeys = new ArgumentWithoutValue(this, -1, "--dumpkeys");
      synchronize = new ArgumentWithValue(this, null, -1, "--synchronize");
      disconnect = new ArgumentWithValue(this, null, -1, "--disconnectsource");
   }

   @Override
   public Collection<CommandArgument> getArguments(CommandContext ctx) {
      List<CommandArgument> arguments = new ArrayList<CommandArgument>(5);
      try {
         ParsedCommandLine parsedCommandLine = ctx.getParsedCommandLine();
         if (!all.isPresent(parsedCommandLine) && parsedCommandLine.getOtherProperties().size() <= 1) {
            if (!dumpKeys.isPresent(parsedCommandLine) && !synchronize.isPresent(parsedCommandLine) && !disconnect.isPresent(parsedCommandLine)) {
               arguments.add(dumpKeys);
               arguments.add(synchronize);
               arguments.add(disconnect);
            }
            arguments.add(all);
            arguments.add(cacheName);
         }
      } catch (CommandFormatException e) {
         //ignored
      }
      return arguments;
   }
}
