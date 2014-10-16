package org.infinispan.commands.read;

import java.util.AbstractCollection;
import java.util.Collection;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorCollection;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.InternalCacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.filter.AcceptAllKeyValueFilter;
import org.infinispan.util.TimeService;

/**
 * Command implementation for {@link java.util.Map#values()} functionality.
 *
 * @author Galder Zamarreño
 * @author Mircea.Markus@jboss.com
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author William Burns
 * @since 4.0
 */
public class ValuesCommand<K, V> extends AbstractLocalCommand implements VisitableCommand {
   private final DataContainer container;
   private final TimeService timeService;
   private final Cache<K, V> cache;

   public ValuesCommand(DataContainer container, TimeService timeService, Cache<K, V> cache, Set<Flag> flags) {
      setFlags(flags);
      this.container = container;
      this.timeService = timeService;
      if (flags != null) {
         this.cache = cache.getAdvancedCache().withFlags(flags.toArray(new Flag[flags.size()]));
      } else {
         this.cache = cache;
      }
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitValuesCommand(ctx, this);
   }

   @Override
   public CloseableIteratorCollection<? extends Object> perform(InvocationContext ctx) throws Throwable {
      String useClusterSize = SecurityActions.getSystemProperty("infinispan.accurate.bulk.ops");
      if (useClusterSize == null || !useClusterSize.equalsIgnoreCase("true")) {
         if (ctx.getLookedUpEntries().isEmpty()) {
            return new ExpiredFilteredValues(container.entrySet(), timeService);
         }

         return new FilteredValues(container, ctx.getLookedUpEntries(), timeService);
      } else {
         return new BackingValuesCollection<K, V>(cache);
      }
   }

   @Override
   public String toString() {
      return "ValuesCommand{" +
            "values=" + container.size() + " elements" +
            '}';
   }

   private static class FilteredValues extends AbstractCollection<Object> implements CloseableIteratorCollection<Object> {
      final DataContainer container;
      final Collection<Object> values;
      final Set<InternalCacheEntry> entrySet;
      final Map<Object, CacheEntry> lookedUpEntries;
      final TimeService timeService;

      FilteredValues(DataContainer container, Map<Object, CacheEntry> lookedUpEntries, TimeService timeService) {
         this.container = container;
         values = container.values();
         entrySet = container.entrySet();
         this.lookedUpEntries = lookedUpEntries;
         this.timeService = timeService;
      }

      @Override
      public int size() {
         long currentTimeMillis = 0;
         Set<Object> validKeys = new HashSet<Object>();
         // First, removed any expired ones
         for (InternalCacheEntry e: entrySet) {
            if (e.canExpire()) {
               if (currentTimeMillis == 0)
                  currentTimeMillis = timeService.wallClockTime();
               if (!e.isExpired(currentTimeMillis)) {
                  validKeys.add(e.getKey());
               }
            } else {
               validKeys.add(e.getKey());
            }
         }

         int size = validKeys.size();
         // Update according to entries added or removed in tx
         for (CacheEntry e: lookedUpEntries.values()) {
            if (validKeys.contains(e.getKey())) {
               if (e.isRemoved()) {
                  size --;
               }
            } else if (!e.isRemoved()) {
               size ++;
            }
         }
         return Math.max(size, 0);
      }

      @Override
      public boolean contains(Object o) {
         for (CacheEntry e: lookedUpEntries.values()) {
            // A value can repeat, so only return true if we find one, not just whether or not it was removed
            if (o.equals(e.getValue()) && !e.isRemoved()) {
               return true;
            }
         }

         for (Map.Entry<Object, Object> entry : container) {
            Object value = entry.getValue();
            // If we find the key looked up in the lookedUpEntries at this point it means that entry was removed
            // however the value could be on another key so keep looking
            if (o.equals(value) && !lookedUpEntries.containsKey(entry.getKey())) {
               return true;
            }
         }
         return false;
      }

      @Override
      public CloseableIterator<Object> iterator() {
         return new Itr();
      }

      @Override
      public boolean add(Object e) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      private class Itr implements CloseableIterator<Object> {

         private final Iterator<CacheEntry> it1 = lookedUpEntries.values().iterator();
         private final Iterator<InternalCacheEntry> it2 = entrySet.iterator();
         private boolean atIt1 = true;
         private Object next;

         Itr() {
            fetchNext();
         }

         private void fetchNext() {
            if (atIt1) {
               boolean found = false;
               while (it1.hasNext()) {
                  CacheEntry e = it1.next();
                  if (!e.isRemoved()) {
                     next = e.getValue();
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  atIt1 = false;
               }
            }

            if (!atIt1) {
               boolean found = false;
               long currentTimeMillis = 0;
               while (it2.hasNext()) {
                  InternalCacheEntry ice = it2.next();
                  Object key = ice.getKey();
                  if (ice.canExpire()) {
                     if (currentTimeMillis == 0)
                        currentTimeMillis = timeService.wallClockTime();
                     if (ice.isExpired(currentTimeMillis))
                        continue;
                  }

                  if (!lookedUpEntries.containsKey(key)) {
                     next = ice.getValue();
                     found = true;
                     break;
                  }
               }

               if (!found) {
                  next = null;
               }
            }
         }

         @Override
         public boolean hasNext() {
            if (next == null) {
               fetchNext();
            }
            return next != null;
         }

         @Override
         public Object next() {
            if (next == null) {
               fetchNext();
            }

            if (next == null) {
               throw new NoSuchElementException();
            }

            Object ret = next;
            next = null;
            return ret;
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }

         @Override
         public void close() {
         }
      }
   }

   public static class ExpiredFilteredValues extends AbstractCollection<Object> implements CloseableIteratorCollection<Object> {
      final Set<InternalCacheEntry> entrySet;
      final TimeService timeService;

      public ExpiredFilteredValues(Set<InternalCacheEntry> entrySet, TimeService timeService) {
         this.entrySet = entrySet;
         this.timeService = timeService;
      }

      @Override
      public CloseableIterator<Object> iterator() {
         return new Itr();
      }

      @Override
      public boolean add(Object e) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean remove(Object o) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean addAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean retainAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public boolean removeAll(Collection<?> c) {
         throw new UnsupportedOperationException();
      }

      @Override
      public void clear() {
         throw new UnsupportedOperationException();
      }

      @Override
      public int size() {
         // Use entry set as a way to calculate the number of values.
         int s = entrySet.size();
         long currentTimeMillis = 0;
         for (InternalCacheEntry e: entrySet) {
            if (e.canExpire()) {
               if (currentTimeMillis==0)
                  currentTimeMillis = timeService.wallClockTime();
               if (e.isExpired(currentTimeMillis))
                  s--;
            }
         }
         return s;
      }

      private class Itr implements CloseableIterator<Object> {

         private final Iterator<InternalCacheEntry> it = entrySet.iterator();
         private Object next;

         private Itr() {
            fetchNext();
         }

         private void fetchNext() {
            long currentTimeMillis = 0;
            while (it.hasNext()) {
               InternalCacheEntry e = it.next();
               final boolean canExpire = e.canExpire();
               if (canExpire && currentTimeMillis == 0) {
                  currentTimeMillis = timeService.wallClockTime();
               }
               if (!canExpire) {
                  next = e.getValue();
                  break;
               } else if (!e.isExpired(currentTimeMillis)) {
                  next = e.getValue();
                  break;
               }
            }
         }

         @Override
         public boolean hasNext() {
            if (next == null)
               fetchNext();

            return next != null;
         }

         @Override
         public Object next() {
            if (next == null)
               fetchNext();

            if (next == null)
               throw new NoSuchElementException();

            Object ret = next;
            next = null;
            return ret;
         }

         @Override
         public void remove() {
            throw new UnsupportedOperationException();
         }

         @Override
         public void close() {
         }
      }
   }

   private static class BackingValuesCollection<K, V> extends AbstractCloseableIteratorCollection<V, K, V> {

      public BackingValuesCollection(Cache<K, V> cache) {
         super(cache);
      }

      @Override
      public CloseableIterator<V> iterator() {
         return new EntryToValueIterator(cache.getAdvancedCache().filterEntries(AcceptAllKeyValueFilter.getInstance()).iterator());
      }

      @Override
      public boolean contains(Object o) {
         // We don't support null values
         if (o == null) {
            throw new NullPointerException();
         }
         CloseableIterator<V> it = iterator();
         try {
            while (it.hasNext())
               if (o.equals(it.next()))
                  return true;
            return false;
         } finally {
            it.close();
         }
      }

      @Override
      public boolean containsAll(Collection<?> c) {
         // The AbstractCollection implementation calls contains for each element.  Instead we want to call the iterator
         // only once so we have a special implementation.
         if (c.size() > 0) {
            Set<?> set = new HashSet<Object>(c);
            CloseableIterator<V> it = iterator();
            try {
               while (!set.isEmpty() && it.hasNext()) {
                  set.remove(it.next());
               }
            } finally {
               it.close();
            }
            return set.isEmpty();
         }
         return true;
      }

      @Override
      public boolean remove(Object o) {
         CloseableIterator<V> it = iterator();
         try {
            while (it.hasNext()) {
               if (o.equals(it.next())) {
                  it.remove();
                  return true;
               }
            }
            return false;
         } finally {
            it.close();
         }
      }
   }

   private static class EntryToValueIterator<V> implements CloseableIterator<V> {

      private final CloseableIterator<CacheEntry> iterator;

      public EntryToValueIterator(CloseableIterator<CacheEntry> iterator) {
         this.iterator = iterator;
      }

      @Override
      public boolean hasNext() {
         return iterator.hasNext();
      }

      @Override
      public V next() {
         return (V) iterator.next().getValue();
      }

      @Override
      public void remove() {
         iterator.remove();
      }

      @Override
      public void close() {
         iterator.close();
      }
   }
}
