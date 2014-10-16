package org.infinispan.iteration.impl;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.impl.TxInvocationContext;
import org.infinispan.filter.Converter;
import org.infinispan.iteration.EntryIterable;

/**
 * {@inheritDoc}
 *
 * @author wburns
 * @since 7.0
 */
public class TransactionAwareEntryIterable<K, V> extends TransactionAwareCloseableIterable<K, V> implements EntryIterable<K, V> {
   private final EntryIterable<K, V> entryIterable;

   public TransactionAwareEntryIterable(EntryIterable<K, V> entryIterable, TxInvocationContext ctx,
                                        Cache<K, V> cache) {
      super(entryIterable, ctx, cache);
      this.entryIterable = entryIterable;
   }

   @Override
   public <C> CloseableIterable<CacheEntry> converter(Converter<? super K, ? super V, ? extends C> converter) {
      return new TransactionAwareCloseableIterable<K, C>(entryIterable.converter(converter), ctx, cache);
   }
}
