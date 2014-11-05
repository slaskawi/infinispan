package org.infinispan.security.actions;

import org.infinispan.AdvancedCache;
import org.infinispan.container.DataContainer;

/**
 * @author Tristan Tarrant
 * @since 7.1
 */
public class GetDataContainerAction extends AbstractAdvancedCacheAction<DataContainer> {

   public GetDataContainerAction(AdvancedCache<?, ?> cache) {
      super(cache);
   }

   @Override
   public DataContainer run() {
      return cache.getDataContainer();
   }

}
