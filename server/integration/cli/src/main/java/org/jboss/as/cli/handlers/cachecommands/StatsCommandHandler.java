package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandArgument;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.cli.util.CliCommandBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#STATS} handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class StatsCommandHandler extends CacheNameArgumentCommandHandler {

   private final ArgumentWithoutValue container;

   public StatsCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.STATS, buffer);
      container = new ArgumentWithoutValue(this, -1, "--container");
   }

   @Override
   public Collection<CommandArgument> getArguments(CommandContext ctx) {
      try {
         ParsedCommandLine parsedCommandLine = ctx.getParsedCommandLine();
         if (!container.isPresent(parsedCommandLine) && parsedCommandLine.getOtherProperties().size() <= 1) {
            return Arrays.<CommandArgument>asList(container, cacheName);
         }
      } catch (CommandFormatException e) {
         //ignored
      }
      return Collections.emptyList();
   }
}
