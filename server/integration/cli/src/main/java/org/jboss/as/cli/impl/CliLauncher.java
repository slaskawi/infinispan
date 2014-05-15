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
package org.jboss.as.cli.impl;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.Arrays;
import java.util.LinkedList;
import java.util.Queue;

import org.jboss.as.cli.CliInitializationException;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandContextFactory;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.handlers.VersionHandler;
import org.jboss.as.cli.util.ConnectionUrl;
import org.jboss.as.cli.util.InfinispanUtil;
import org.jboss.as.protocol.StreamUtils;

/**
 *
 * @author Alexey Loubyansky
 */
public class CliLauncher {

    public static void main(String[] args) throws Exception {
        int exitCode = 0;
        CommandContext cmdCtx = null;
        try {
            String argError = null;
            File file = null;
            boolean connect = false;
            boolean version = false;
            boolean noLocalAuth = false;
            int connectionTimeout = -1;
            boolean help = false;
            ConnectionUrl connectionUrl = ConnectionUrl.DEFAULT;

            final Queue<String> arguments = new LinkedList<String>(Arrays.asList(args));
            while(!arguments.isEmpty()) {
                String arg = arguments.poll();
                if(arg.startsWith("--connect") || "-c".equals(arg)) {
                    final String value = extractValue(arguments, arg, "connect", "c");
                    connectionUrl = ConnectionUrl.parse(value);
                    connect = true;
                    noLocalAuth = connectionUrl.getUser() != null;
                } else if("--version".equals(arg) || "-v".equals(arg)) {
                    version = true;
                } else if(arg.startsWith("--file") || "-f".equals(arg)) {
                    if(file != null) {
                        argError = "Duplicate argument '--file'.";
                        break;
                    }

                    final String fileName = extractValue(arguments, arg, "file", "f");
                    if(!fileName.isEmpty()) {
                        file = new File(fileName);
                        if(!file.exists()) {
                            argError = "File " + file.getAbsolutePath() + " doesn't exist.";
                            break;
                        }
                    } else {
                        argError = "Argument '--file' is missing value.";
                        break;
                    }
                } else if (arg.equals("--help") || arg.equals("-h")) {
                    help = true;
                } else {
                    argError = "Unknown argument " + arg;
                    break;
                }
            }

            if(argError != null) {
                System.err.println(argError);
                exitCode = 1;
                return;
            }

            if (help) {
                cmdCtx = initCommandContext(connectionUrl, noLocalAuth, false, connect, connectionTimeout);
                cmdCtx.printLine("Usage: ispn-cli [OPTION]...");
                cmdCtx.printLine("Command-line interface for interacting with a running instance of Infinispan");
                cmdCtx.printLine("");
                cmdCtx.printLine("Options:");
                cmdCtx.printLine(" -c, --connect=URL    connects to a running instance of Infinispan.");
                cmdCtx.printLine("                      JMX over RMI jmx://[username[:password]]@host:port[/container[/cache]]");
                cmdCtx.printLine("                      JMX over JBoss remoting remoting://[username[:password]]@host:port[/container[/cache]]");
                cmdCtx.printLine(" -f, --file=FILE      reads input from the specified file instead of using interactive mode.");
                cmdCtx.printLine(" -h, --help           shows this help page.");
                cmdCtx.printLine(" -v, --version        shows version information.");
                return;
            }

            if(version) {
                cmdCtx = initCommandContext(connectionUrl, noLocalAuth, false, connect, connectionTimeout);
                VersionHandler.INSTANCE.handle(cmdCtx);
                return;
            }

            if(file != null) {
                cmdCtx = initCommandContext(connectionUrl, noLocalAuth, false, connect, connectionTimeout);
                processFile(file, cmdCtx);
                return;
            }

            // Interactive mode
            cmdCtx = initCommandContext(connectionUrl, noLocalAuth, true, connect, connectionTimeout);
            cmdCtx.interact();
        } catch(Throwable t) {
            t.printStackTrace();
            exitCode = 1;
        } finally {
            if(cmdCtx != null && cmdCtx.getExitCode() != 0) {
                exitCode = cmdCtx.getExitCode();
            }
        }
        System.exit(exitCode);
    }

    private static String extractValue(Queue<String> args, String arg, String key, String shortKey) {
       //formats: --<key>=<value> or --<key> <value> or -<skey> <value>
       if (arg.equals("--" + key) || arg.equals("-" + shortKey)) {
          //--<key> <value> or -<skey> <value>
          return args.isEmpty() ? "" : args.poll();
       } else {
          //--<key>=<value>
          return arg.substring(key.length() + 3);
       }
    }

    private static CommandContext initCommandContext(ConnectionUrl connectionUrl, boolean disableLocalAuth, boolean initConsole, boolean connect, final int connectionTimeout) throws Exception {
        final CommandContext cmdCtx = CommandContextFactory.getInstance().newCommandContext(connectionUrl.getHost(), connectionUrl.getPort(), connectionUrl.getUser(), connectionUrl.getPass(), disableLocalAuth, initConsole, connectionTimeout);
        if(connect) {
            try {
                cmdCtx.connectController();
                InfinispanUtil.connect(cmdCtx, connectionUrl.getContainer(), connectionUrl.getCache());
            } catch (CommandLineException e) {
                throw new CliInitializationException("Failed to connect to the controller", e);
            }
        }
        return cmdCtx;
    }


    private static void processFile(File file, final CommandContext cmdCtx) {

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(file));
            String line = reader.readLine();
            while (cmdCtx.getExitCode() == 0 && !cmdCtx.isTerminated() && line != null) {
                cmdCtx.handleSafe(line.trim());
                line = reader.readLine();
            }
        } catch (Throwable e) {
            throw new IllegalStateException("Failed to process file '" + file.getAbsolutePath() + "'", e);
        } finally {
            StreamUtils.safeClose(reader);
            cmdCtx.terminateSession();
        }
    }
}
