package org.jboss.as.cli.handlers;

import org.jboss.as.cli.CliEvent;
import org.jboss.as.cli.CliEventListener;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.Util;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.operation.OperationRequestAddress;
import org.jboss.as.cli.operation.impl.DefaultCallbackHandler;

import java.util.List;

/**
 * {@link org.jboss.as.cli.CommandHandler} implementation with the {@code container} command logic.
 * <p/>
 * The {@code container} command changes the container in which the Infinispan CLI command are executed against.
 *
 * @author Pedro Ruivo
 * @since 6.0
 */
public class ContainerCommandHandler extends CommandHandlerWithArguments implements CliEventListener {

   private static final String INFINISPAN_SUBSYSTEM = "/subsystem=infinispan";
   private static final String CONTAINER_TYPE = "cache-container";
   private static final String CONTAINER_ADDRESS = INFINISPAN_SUBSYSTEM + "/" + CONTAINER_TYPE;

   private boolean connect;

   public ContainerCommandHandler(CommandContext ctx) {
      super();
      ctx.addEventListener(this);
      this.connect = false;
   }

   @Override
   public boolean isAvailable(CommandContext ctx) {
      return connect;
   }

   @Override
   public boolean isBatchMode(CommandContext ctx) {
      return false;
   }

   @Override
   public void handle(CommandContext ctx) throws CommandLineException {
      final List<String> containersName = getContainerNames(ctx);
      if (ctx.getArgumentsString() == null) {
         ctx.printColumns(containersName);
      } else {
         if (!containersName.contains(ctx.getArgumentsString())) {
            throw new IllegalArgumentException(ctx.getArgumentsString());
         }
         setContainerInContext(ctx, ctx.getArgumentsString());
      }
   }

   @Override
   public void cliEvent(CliEvent event, CommandContext ctx) {
      switch (event) {
         case CONNECTED:
            this.connect = true;
            try {
               List<String> containersName = getContainerNames(ctx);
               if (!containersName.isEmpty()) {
                  setContainerInContext(ctx, containersName.get(0));
               }
            } catch (OperationFormatException e) {
               //it shouldn't happen!
               throw new IllegalStateException(e);
            }
            break;
         case DISCONNECTED:
            this.connect = false;
            setContainerInContext(ctx, null);
            break;
      }
   }

   private void setContainerInContext(CommandContext context, String container) {
      if (container == null) {
         context.remove("container");
      } else {
         context.set("container", CONTAINER_ADDRESS + "=" + container);
      }
   }

   private static List<String> getContainerNames(CommandContext ctx) throws OperationFormatException {
      DefaultCallbackHandler handler = new DefaultCallbackHandler();
      ctx.getCommandLineParser().parse(INFINISPAN_SUBSYSTEM, handler);
      OperationRequestAddress address = handler.getAddress();
      return Util.getNodeNames(ctx.getModelControllerClient(), address, CONTAINER_TYPE);
   }
}
