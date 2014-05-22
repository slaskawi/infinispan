package org.infinispan.filter;

import org.infinispan.persistence.spi.AdvancedCacheLoader;

/**
 * This class is to be used to bridge the 2 key filter classes that are provided to allow for
 *
 *
 * @author wburns
 * @since JDG 6.3
 */
public class KeyFilterBridge<K> implements AdvancedCacheLoader.KeyFilter<K> {
   private final KeyFilter<? super K> filter;

   public KeyFilterBridge(KeyFilter<? super K> filter) {
      this.filter = filter;
   }
   @Override
   public boolean shouldLoadKey(K key) {
      return filter.accept(key);
   }
}
