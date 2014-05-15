package org.jboss.as.cli.handlers;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandFormatException;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.CommandRegistry;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.OperationRequestAddress;
import org.jboss.as.cli.operation.impl.DefaultCallbackHandler;
import org.jboss.as.cli.util.CliCommandBuffer;
import org.jboss.as.cli.util.InfinispanUtil;
import org.jboss.dmr.ModelNode;

/**
 * {@link org.jboss.as.cli.CommandHandler} implementation to handle Infinispan CLI commands.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class CliInterpreterCommandHandler extends BaseOperationCommand {

   private final CacheCommand cacheCommand;
   private final CliCommandBuffer buffer;

   private CliInterpreterCommandHandler(CommandContext ctx, CacheCommand cacheCommand, CliCommandBuffer buffer) {
      super(ctx, cacheCommand.name, true);
      this.cacheCommand = cacheCommand;
      this.buffer = buffer;
   }

   public static void registerCommands(CommandRegistry commandRegistry, CommandContext context) {
      CliCommandBuffer buffer = new CliCommandBuffer();
      for (CacheCommand command : CacheCommand.values()) {
         commandRegistry.registerHandler(new CliInterpreterCommandHandler(context, command, buffer), true, command.name);
      }
   }

   @Override
   public boolean isAvailable(CommandContext ctx) {
      return super.isAvailable(ctx) && InfinispanUtil.getCacheInfo(ctx).getContainer() != null;
   }

   @Override
   public boolean isBatchMode(CommandContext ctx) {
      return false;
   }

   @Override
   protected void recognizeArguments(CommandContext ctx) throws CommandFormatException {
      //no-op, accept everything! the interpreter is the only who knows the arguments!
   }

   @Override
   protected void doHandle(CommandContext ctx) throws CommandLineException {
      if (buffer.append(buildCommandString(ctx), cacheCommand.nesting)) {
         super.doHandle(ctx);
      }
   }

   @Override
   protected ModelNode buildRequestWithoutHeaders(CommandContext ctx) throws CommandFormatException {
      try {
         return InfinispanUtil.buildCliRequest(ctx, buffer.getCommandAndReset());
      } catch (CommandLineException e) {
         throw new CommandFormatException(e);
      }
   }

   @Override
   protected void handleResponse(CommandContext ctx, ModelNode response, boolean composite) throws CommandLineException {
      if (cacheCommand == CacheCommand.CACHE) {
         InfinispanUtil.changeToCache(ctx, InfinispanUtil.getCacheInfo(ctx).getContainer(), ctx.getArgumentsString());
      }

      if (!response.has(Util.RESULT)) {
         return;
      }
      ModelNode result = response.get(Util.RESULT);
      InfinispanUtil.updateStateFromResponse(result, ctx);

      if (!result.has("result")) {
         return;
      }
      ctx.printLine(result.get("result").asString());
   }

   private String buildCommandString(CommandContext ctx) {
      StringBuilder command = new StringBuilder(cacheCommand.name);
      if (ctx.getArgumentsString() != null) {
         command.append(' ').append(ctx.getArgumentsString());
      }
      command.append('\n');
      return command.toString();
   }

   private static OperationRequestAddress buildOperationRequest(CommandContext ctx, String containerAddress) throws OperationFormatException {
      DefaultCallbackHandler handler = new DefaultCallbackHandler();
      ctx.getCommandLineParser().parse(containerAddress, handler);
      return handler.getAddress();
   }

   public static enum CacheCommand {
      ABORT("abort", -1),
      BEGIN("begin", 1),
      CACHE("cache"),
      CLEAR("clear"),
      COMMIT("commit", -1),
      CREATE("create"),
      ENCODING("encoding"),
      END("end", -1),
      EVICT("evict"),
      GET("get"),
      INFO("info"),
      LOCATE("locate"),
      PUT("put"),
      REMOVE("remove"),
      REPLACE("replace"),
      ROLLBACK("rollback", -1),
      SITE("site"),
      START("start", 1),
      STATS("stats"),
      UPGRADE("upgrade"),
      VERSION("version");
      private final String name;
      private final int nesting;

      CacheCommand(String name) {
         this(name, 0);
      }

      CacheCommand(String name, int nesting) {
         this.name = name;
         this.nesting = nesting;
      }
   }
}
