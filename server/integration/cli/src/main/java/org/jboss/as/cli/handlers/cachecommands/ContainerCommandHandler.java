package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.handlers.CommandHandlerWithArguments;
import org.jboss.as.cli.impl.ArgumentWithValue;
import org.jboss.as.cli.impl.DefaultCompleter;
import org.jboss.as.cli.operation.OperationFormatException;
import org.jboss.as.cli.util.InfinispanUtil;

import java.util.Collection;
import java.util.Collections;

/**
 * {@link org.jboss.as.cli.CommandHandler} implementation with the {@code container} command logic.
 * <p/>
 * The {@code container} command changes the container in which the Infinispan CLI command are executed against. The
 * command is only executed in the client.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class ContainerCommandHandler extends CommandHandlerWithArguments {

   private final ArgumentWithValue containerName;

   public ContainerCommandHandler() {
      super();
      containerName = new ArgumentWithValue(this, new DefaultCompleter(new DefaultCompleter.CandidatesProvider() {
         @Override
         public Collection<String> getAllCandidates(CommandContext ctx) {
            try {
               return InfinispanUtil.getContainerNames(ctx);
            } catch (OperationFormatException e) {
               return Collections.emptyList();
            }
         }
      }), 0, "--container-name");
   }

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
      recognizeArguments(ctx);
      String containerName = this.containerName.getValue(ctx.getParsedCommandLine());
      if (containerName == null) {
         ctx.printColumns(InfinispanUtil.getContainerNames(ctx));
      } else {
         InfinispanUtil.changeToContainer(ctx, containerName);
      }
   }
}
