package org.jboss.as.cli.handlers;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 6.0
 */
public class UnsupportedCommandHandler extends CommandHandlerWithArguments {


   @Override
   public boolean isAvailable(CommandContext ctx) {
      return true;
   }

   @Override
   public boolean isBatchMode(CommandContext ctx) {
      return false;
   }

   @Override
   public void handle(CommandContext ctx) throws CommandLineException {
      ctx.printLine("Unsupported command.");
   }
}
