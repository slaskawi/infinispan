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


import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.operation.ParsedCommandLine;

import java.util.List;

/**
 * Disconnect handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class DisconnectHandler extends CommandHandlerWithHelp {

    public DisconnectHandler() {
        this("disconnect");
    }

    public DisconnectHandler(String command) {
        super(command);
    }

    @Override
    public boolean hasArgument(CommandContext ctx, int index) {
        return false;
    }

    @Override
    protected void recognizeArguments(CommandContext ctx) throws CommandFormatException {
        final ParsedCommandLine parsedCmd = ctx.getParsedCommandLine();
        if(parsedCmd.getOtherProperties().size() > 0) {
            throw new CommandFormatException("The command accepts zero argument but received: " + parsedCmd.getOtherProperties());
        }
    }

   @Override
   public boolean isAvailable(CommandContext ctx) {
      return ctx.getModelControllerClient() != null;
   }

   @Override
    protected void doHandle(CommandContext ctx) throws CommandLineException {
       ctx.disconnectController();
    }
}
