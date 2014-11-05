package org.infinispan.security.actions;

import org.infinispan.AdvancedCache;
import org.infinispan.distexec.mapreduce.MapReduceTask;

/**
 * @author Tristan Tarrant
 * @since 6.2
 */
public class NewMapReduceTaskAction extends AbstractAdvancedCacheAction<MapReduceTask> {

   public NewMapReduceTaskAction(AdvancedCache<?, ?> cache) {
      super(cache);
   }

   @Override
   public MapReduceTask run() {
      return new MapReduceTask(cache);
   }

}
