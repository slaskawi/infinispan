package org.infinispan.commands.read;

import java.util.AbstractSet;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;
import java.util.NoSuchElementException;
import java.util.Set;

import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.commons.util.CloseableIteratorSet;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.filter.AcceptAllKeyValueFilter;
import org.infinispan.filter.NullValueConverter;

/**
 * Command implementation for {@link java.util.Map#keySet()} functionality.
 *
 * @author Galder Zamarreño
 * @author Mircea.Markus@jboss.com
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @author William Burns
 * @since 4.0
 */
public class KeySetCommand<K, V> extends AbstractLocalCommand implements VisitableCommand {
   private final DataContainer container;
   private final Cache<K, V> cache;

   public KeySetCommand(DataContainer container, Cache<K, V> cache, Set<Flag> flags) {
      setFlags(flags);
      this.container = container;
      if (flags != null) {
         this.cache = cache.getAdvancedCache().withFlags(flags.toArray(new Flag[flags.size()]));
      } else {
         this.cache = cache;
      }
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitKeySetCommand(ctx, this);
   }

   @Override
   public CloseableIteratorSet<? extends Object> perform(InvocationContext ctx) throws Throwable {
      String useClusterSize = SecurityActions.getSystemProperty("infinispan.accurate.bulk.ops");
      if (useClusterSize == null || !useClusterSize.equalsIgnoreCase("true")) {
         Set<Object> objects = container.keySet();
         if (ctx.getLookedUpEntries().isEmpty()) {
            return new ExpiredFilteredKeySet(objects, container);
         }

         return new FilteredKeySet(objects, ctx.getLookedUpEntries(), container);
      } else {
         return new BackingKeySet<K, V>(cache);
      }
   }

   @Override
   public String toString() {
      return "KeySetCommand{" +
            "cache=" + cache.getName() +
            '}';
   }

   private static class BackingKeySet<K, V> extends AbstractCloseableIteratorCollection<K, K, V> implements CloseableIteratorSet<K> {

      public BackingKeySet(Cache<K, V> cache) {
         super(cache);
      }

      @Override
      public CloseableIterator<K> iterator() {
         return new EntryToKeyIterator(cache.getAdvancedCache().filterEntries(AcceptAllKeyValueFilter.getInstance())
                                             .converter(NullValueConverter.getInstance()).iterator());
      }

      @Override
      public boolean contains(Object o) {
         return cache.containsKey(o);
      }

      @Override
      public boolean remove(Object o) {
         return cache.remove(o) != null;
      }
   }

   private static class FilteredKeySet extends AbstractSet<Object> implements CloseableIteratorSet<Object> {
      final Set<Object> keySet;
      final Map<Object, CacheEntry> lookedUpEntries;
      final DataContainer container;

      FilteredKeySet(Set<Object> keySet, Map<Object, CacheEntry> lookedUpEntries, DataContainer container) {
         this.keySet = keySet;
         this.lookedUpEntries = lookedUpEntries;
         this.container = container;
      }

      @Override
      public int size() {
         int size = keySet.size();
         // First, removed any expired keys
         for (Object k : keySet) {
            // Given the key set, a key won't be contained if it's expired
            if (!container.containsKey(k))
               size--;
         }
         // Update according to keys added or removed in tx
         for (CacheEntry e: lookedUpEntries.values()) {
            if (container.containsKey(e.getKey())) {
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
         CacheEntry e = lookedUpEntries.get(o);
         if (e != null) {
            return !e.isRemoved();
         }
         return keySet.contains(o);
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
         private final Iterator<Object> it2 = keySet.iterator();
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
                     next = e.getKey();
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
               while (it2.hasNext()) {
                  Object k = it2.next();
                  if (!lookedUpEntries.containsKey(k)) {
                     next = k;
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

   public static class ExpiredFilteredKeySet extends AbstractSet<Object> implements CloseableIteratorSet<Object> {
      final Set<Object> keySet;
      final DataContainer container;

      public ExpiredFilteredKeySet(Set<Object> keySet, DataContainer container) {
         this.keySet = keySet;
         this.container = container;
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
      public CloseableIterator<Object> iterator() {
         return new Itr();
      }

      @Override
      public int size() {
         // Size cannot be cached because even if the set is immutable,
         // over time, the expired entries could grow hence reducing the size
         int s = keySet.size();
         for (Object k : keySet) {
            // Given the key set, a key won't be contained if it's expired
            if (!container.containsKey(k))
               s--;
         }
         return s;
      }

      private class Itr implements CloseableIterator<Object> {

         private final Iterator<Object> it = keySet.iterator();
         private Object next;

         private Itr() {
            fetchNext();
         }

         private void fetchNext() {
            while (it.hasNext()) {
               Object k = it.next();
               if (container.containsKey(k)) {
                  next = k;
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

   private static class EntryToKeyIterator<K> implements CloseableIterator<K> {

      private final CloseableIterator<CacheEntry> iterator;

      public EntryToKeyIterator(CloseableIterator<CacheEntry> iterator) {
         this.iterator = iterator;
      }

      @Override
      public boolean hasNext() {
         return iterator.hasNext();
      }

      @Override
      public K next() {
         return (K) iterator.next().getKey();
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
