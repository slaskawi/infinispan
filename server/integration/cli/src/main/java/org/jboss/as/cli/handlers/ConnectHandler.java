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


import java.util.List;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.operation.ParsedCommandLine;
import org.jboss.as.cli.util.ConnectionUrl;
import org.jboss.as.cli.util.InfinispanUtil;

/**
 * Connect handler.
 *
 * @author Alexey Loubyansky
 */
public class ConnectHandler extends CommandHandlerWithHelp {

    public ConnectHandler() {
        this("connect");
    }

    public ConnectHandler(String command) {
        super(command);
    }

    @Override
    public boolean hasArgument(CommandContext ctx, int index) {
        return index <= 1;
    }

    @Override
    protected void recognizeArguments(CommandContext ctx) throws CommandFormatException {
        final ParsedCommandLine parsedCmd = ctx.getParsedCommandLine();
        if(parsedCmd.getOtherProperties().size() > 1) {
            throw new CommandFormatException("The command accepts only one argument but received: " + parsedCmd.getOtherProperties());
        }
    }

    @Override
    protected void doHandle(CommandContext ctx) throws CommandLineException {
        ConnectionUrl connectionUrl = ConnectionUrl.DEFAULT;
        final ParsedCommandLine parsedCmd = ctx.getParsedCommandLine();
        final List<String> args = parsedCmd.getOtherProperties();

        if(!args.isEmpty()) {
            if(args.size() != 1) {
                throw new CommandFormatException("The command expects only one argument but got " + args);
            }
            final String arg = args.get(0);
            connectionUrl = ConnectionUrl.parse(arg);
        }

        ctx.connectController(connectionUrl.getHost(), connectionUrl.getPort(), connectionUrl.getUser(), connectionUrl.getPass());
        try {
            InfinispanUtil.connect(ctx, connectionUrl.getContainer(), connectionUrl.getCache());
        } catch (CommandLineException e) {
           ctx.disconnectController();
           throw e;
        } catch (Exception e) {
            ctx.disconnectController();
            throw new CommandLineException(e);
        }
    }
}
