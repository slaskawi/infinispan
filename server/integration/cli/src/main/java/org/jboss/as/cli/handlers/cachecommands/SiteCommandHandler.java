package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandArgument;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.cli.util.CliCommandBuffer;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#SITE} handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class SiteCommandHandler extends NoArgumentsCliCommandHandler {

   private final List<? extends CommandArgument> arguments;

   public SiteCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.SITE, buffer);
      arguments = Arrays.asList(
            new ArgumentWithoutValue(this, -1, "--status"),
            new ArgumentWithoutValue(this, -1, "--online"),
            new ArgumentWithoutValue(this, -1, "--offline"),
            new ArgumentWithoutValue(this, -1, "--push"),
            new ArgumentWithoutValue(this, -1, "--cancelpush"),
            new ArgumentWithoutValue(this, -1, "--cancelreceive"),
            new ArgumentWithoutValue(this, -1, "--pushstatus"),
            new ArgumentWithoutValue(this, -1, "--clearpushstatus"),
            new ArgumentWithoutValue(this, -1, "--sendingsite")
      );
      new ArgumentWithValue(this, null, 0, "--site-name");
   }

   @Override
   public Collection<CommandArgument> getArguments(CommandContext ctx) {
      ParsedCommandLine parsedCommandLine = ctx.getParsedCommandLine();
      try {
         for (CommandArgument argument : arguments) {
            if (argument.isPresent(parsedCommandLine)) {
               return Collections.emptyList();
            }
         }
         return Collections.unmodifiableCollection(arguments);
      } catch (CommandFormatException e) {
         //ignored!
      }
      return Collections.emptyList();
   }
}
