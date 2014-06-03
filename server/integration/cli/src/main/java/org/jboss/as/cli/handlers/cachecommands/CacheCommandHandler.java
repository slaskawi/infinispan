package org.jboss.as.cli.handlers.cachecommands;

import org.jboss.as.cli.CommandContext;
import org.jboss.as.cli.CommandLineException;
import org.jboss.as.cli.util.CliCommandBuffer;
import org.jboss.as.cli.util.InfinispanUtil;
import org.jboss.dmr.ModelNode;

/**
 * The {@link org.jboss.as.cli.handlers.cachecommands.CacheCommand#CACHE} handler.
 *
 * @author Pedro Ruivo
 * @since 6.1
 */
public class CacheCommandHandler extends CacheNameArgumentCommandHandler {

   public CacheCommandHandler(CliCommandBuffer buffer) {
      super(CacheCommand.CACHE, buffer);
   }

   @Override
   protected void printResult(ModelNode result, CommandContext context) throws CommandLineException {
      InfinispanUtil.changeToCache(context, InfinispanUtil.getCacheInfo(context).getContainer(),
                                   cacheName.getValue(context.getParsedCommandLine()));
      super.printResult(result, context);
   }
}
