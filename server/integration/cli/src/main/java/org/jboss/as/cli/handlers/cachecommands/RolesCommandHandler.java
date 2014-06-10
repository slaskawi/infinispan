package org.jboss.as.cli.handlers.cachecommands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import org.jboss.as.cli.CommandArgument;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.cli.util.CliCommandBuffer;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#ROLES} handler.
 *
 * @author Tristan Tarrant
 * @since 6.1
 */
public class RolesCommandHandler extends NoArgumentsCliCommandHandler {

    private ArgumentWithValue principal;

    public RolesCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.ROLES, buffer);
      principal = new ArgumentWithValue(this, null, 0, "--principal");
    }

    @Override
    public Collection<CommandArgument> getArguments(CommandContext ctx) {
       List<CommandArgument> argumentList = new ArrayList<CommandArgument>(1);
       try {
           ParsedCommandLine parsedCommandLine = ctx.getParsedCommandLine();
           int size = parsedCommandLine.getOtherProperties().size();
           if (!principal.isPresent(parsedCommandLine) && size == 0) {
              argumentList.add(principal);
           }
        } catch (CommandFormatException e) {
           //ignored!
        }
       return argumentList;
    }

    private static boolean addIfMissing(ParsedCommandLine parsedCommandLine, String name, int index) {
        int size = parsedCommandLine.getOtherProperties().size();
        String lastProperty = parsedCommandLine.getLastParsedPropertyValue();
        return (size == index && lastProperty == null) ||
              (size == index + 1 && lastProperty != null && name.startsWith(lastProperty));
     }
}
