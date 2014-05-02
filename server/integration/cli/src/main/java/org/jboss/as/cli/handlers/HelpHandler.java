/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */
package org.jboss.as.cli.handlers;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandHandler;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.CommandRegistry;
import org.jboss.as.cli.impl.ArgumentWithoutValue;
import org.jboss.as.protocol.StreamUtils;

/**
 * Help command handler. Reads 'help/help.txt' and prints its content to the output stream.
 *
 * @author Alexey Loubyansky
 */
public class HelpHandler extends CommandHandlerWithHelp {

    private final CommandRegistry cmdRegistry;
    private final ArgumentWithoutValue commands = new ArgumentWithoutValue(this, "--commands");

    public HelpHandler(CommandRegistry cmdRegistry) {
        this("help", cmdRegistry);
    }

    public HelpHandler(String command, CommandRegistry cmdRegistry) {
        super(command);
        if(cmdRegistry == null) {
            throw new IllegalArgumentException("CommandRegistry is null");
        }
        this.cmdRegistry = cmdRegistry;
        // trick to disable the help arg
        helpArg.setExclusive(false);
        helpArg.addCantAppearAfter(commands);
        helpArg.addRequiredPreceding(commands);
    }

    /* (non-Javadoc)
     * @see org.jboss.as.cli.CommandHandler#handle(org.jboss.as.cli.Context)
     */
    @Override
    public void handle(CommandContext ctx) throws CommandLineException {
        boolean printCommands;
        try {
            printCommands = commands.isPresent(ctx.getParsedCommandLine());
        } catch (CommandFormatException e) {
            throw new CommandFormatException(e.getLocalizedMessage());
        }
      String cacheCommand = getCacheCommandArgument(ctx);

        if(printCommands) {
            final List<String> commands = new ArrayList<String>();
            for(String cmd : cmdRegistry.getTabCompletionCommands()) {
                CommandHandler handler = cmdRegistry.getCommandHandler(cmd);
                if(handler.isAvailable(ctx)) {
                    commands.add(cmd);
                }
            }
            Collections.sort(commands);

            ctx.printLine("Commands available in the current context:");
            ctx.printColumns(commands);
            ctx.printLine("To read a description of a specific command execute 'command_name --help'.");
        } else if (cacheCommand != null) {
            printCacheCommandHelp(ctx, cacheCommand);
        } else {
            printHelp(ctx);
        }
    }

    @Override
    protected void doHandle(CommandContext ctx) {
    }

   //for backwards compatibility
   private String getCacheCommandArgument(CommandContext context) {
      String args = context.getArgumentsString();
      if (args == null) {
         return null;
      }
      return args.split("\\s+", 2)[0];
   }

   private void printCacheCommandHelp(CommandContext ctx, String command) throws CommandFormatException {
      String filePath = "help/" + command + ".txt";
      InputStream helpInput = SecurityActions.getClassLoader(CommandHandlerWithHelp.class)
            .getResourceAsStream(filePath);
      if (helpInput != null) {
         BufferedReader reader = new BufferedReader(new InputStreamReader(helpInput));
         try {
            String helpLine = reader.readLine();
            while (helpLine != null) {
               prettyPrintLine(helpLine, ctx);
               helpLine = reader.readLine();
            }
         } catch (java.io.IOException e) {
            throw new CommandFormatException("Failed to read " + filePath + ": " + e.getLocalizedMessage());
         } finally {
            StreamUtils.safeClose(reader);
         }
      } else {
         throw new CommandFormatException("Failed to locate command description " + command);
      }
   }

   private void prettyPrintLine(String line, CommandContext ctx) {
      if (line.isEmpty()) {
         ctx.printLine("");
         return;
      }
      int width = ctx.getTerminalWidth() - 8;
      if (width <= 0) {
         width = 72 - 8;
      }
      int ident = 0;
      for (int i = 0; i < line.length(); ++i) {
         if (line.charAt(i) == ' ') {
            ident ++;
         } else {
            break;
         }
      }
      line = line.substring(ident);

      StringBuilder builder = new StringBuilder();
      for (int i = 0; i < ident; ++i) {
         builder.append(' ');
      }

      while (line.length() > 0) {
         if (builder.length() + line.length() <= width) {
            builder.append(line);
            ctx.printLine(builder.toString());
            return;
         }

         int lastSpace = line.lastIndexOf(' ', width - builder.length());
         if (lastSpace > 0) {
            builder.append(line.substring(0, lastSpace));
            ctx.printLine(builder.toString());
            builder = new StringBuilder();
            for (int i = 0; i < ident; ++i) {
               builder.append(' ');
            }
            line = line.substring(lastSpace + 1);
         } else {
            ctx.printLine("");
            builder = new StringBuilder();
            for (int i = 0; i < ident; ++i) {
               builder.append(' ');
            }
            builder.append(line);
            ctx.printLine(builder.toString());
            return;
         }
      }
      if (builder.length() > 0) {
         ctx.printLine(builder.toString());
      }
    }
}
