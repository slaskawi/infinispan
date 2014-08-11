package org.infinispan.iteration;

import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.filter.Converter;
import org.infinispan.filter.KeyValueFilter;
import org.infinispan.util.concurrent.ConcurrentHashSet;

import java.io.IOException;
import java.util.EnumSet;
import java.util.Iterator;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * CloseableIterable that also tracks the iterators it spawns so it can properly close them when the close method is
 * invoked.
 *
 * @author wburns
 * @since 7.0
 */
public class TrackingEntryIterable<K, V, C> implements CloseableIterable<CacheEntry> {
   protected final EntryRetriever<K, V> entryRetriever;
   protected final KeyValueFilter<? super K, ? super V> filter;
   protected final Converter<? super K, ? super V, ? extends C> converter;
   protected final EnumSet<Flag> flags;
   protected final AtomicBoolean closed = new AtomicBoolean(false);
   protected final Set<CloseableIterator<CacheEntry>> iterators =
         new ConcurrentHashSet<CloseableIterator<CacheEntry>>();

   public TrackingEntryIterable(EntryRetriever<K, V> retriever, KeyValueFilter<? super K, ? super V> filter,
                                 Converter<? super K, ? super V, ? extends C> converter, EnumSet<Flag> flags) {
      if (retriever == null) {
         throw new NullPointerException("Retriever cannot be null!");
      }
      if (filter == null) {
         throw new NullPointerException("Filter cannot be null!");
      }
      this.entryRetriever = retriever;
      this.filter = filter;
      this.converter = converter;
      this.flags = flags;
   }

   @Override
   public void close() throws IOException {
      closed.set(true);
      for (CloseableIterator<CacheEntry> iterator : iterators) {
         iterator.close();
      }
   }

   @Override
   public Iterator<CacheEntry> iterator() {
      if (closed.get()) {
         throw new IllegalStateException("Iterable has been closed - cannot be reused");
      }
      CloseableIterator<CacheEntry> iterator = entryRetriever.retrieveEntries(filter, converter, flags, null);
      iterators.add(iterator);
      // Note we have to check if we were closed afterwards just in case if a concurrent close occurred.
      if (closed.get()) {
         // Rely on fact that multiple closes don't have adverse effects
         try {
            iterator.close();
         } catch (IOException e) {
            // This exception should never be thrown
         }
         throw new IllegalStateException("Iterable has been closed - cannot be reused");
      }
      return iterator;
   }
}
