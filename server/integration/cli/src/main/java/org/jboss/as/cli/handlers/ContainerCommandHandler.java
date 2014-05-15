package org.jboss.as.cli.handlers;

import org.jboss.as.cli.CliEventListener;
import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.util.InfinispanUtil;

/**
 * {@link org.jboss.as.cli.CommandHandler} implementation with the {@code container} command logic.
 * <p/>
 * The {@code container} command changes the container in which the Infinispan CLI command are executed against.
 *
 * @author Pedro Ruivo
 * @since 6.0
 */
public class ContainerCommandHandler extends CommandHandlerWithArguments {

   @Override
   public boolean isAvailable(CommandContext ctx) {
      return ctx.getModelControllerClient() != null;
   }

   @Override
   public boolean isBatchMode(CommandContext ctx) {
      return false;
   }

   @Override
   public void handle(CommandContext ctx) throws CommandLineException {
      if (ctx.getArgumentsString() == null) {
         ctx.printColumns(InfinispanUtil.getContainerNames(ctx));
      } else {
         InfinispanUtil.changeToContainer(ctx, ctx.getArgumentsString());
      }
   }
}
