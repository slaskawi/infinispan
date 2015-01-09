package org.infinispan.security.actions;

import org.infinispan.AdvancedCache;
import org.infinispan.distexec.mapreduce.MapReduceTask;

/**
 * CreateMapReduceTaskAction.
 * 
 * @author Tristan Tarrant
 */
public class CreateMapReduceTaskAction<KIn, VIn, KOut, VOut> extends AbstractAdvancedCacheAction<MapReduceTask<KIn, VIn, KOut, VOut>> {

   public CreateMapReduceTaskAction(final AdvancedCache<KIn, VIn> cache) {
      super(cache);
   }

   @Override
   public MapReduceTask<KIn, VIn, KOut, VOut> run() {
      return new MapReduceTask(cache);
   }

}
