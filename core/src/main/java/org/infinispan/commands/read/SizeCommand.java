package org.infinispan.commands.read;

import org.infinispan.Cache;
import org.infinispan.commands.VisitableCommand;
import org.infinispan.commands.Visitor;
import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.container.DataContainer;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.context.InvocationContext;
import org.infinispan.filter.AcceptAllKeyValueFilter;
import org.infinispan.filter.NullValueConverter;

import java.io.IOException;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;

/**
 * Command to calculate the size of the cache
 *
 * @author Manik Surtani (<a href="mailto:manik@jboss.org">manik@jboss.org</a>)
 * @author Mircea.Markus@jboss.com
 * @author <a href="http://gleamynode.net/">Trustin Lee</a>
 * @since 4.0
 */
public class SizeCommand extends AbstractLocalCommand implements VisitableCommand {
   private final DataContainer container;
   private final Cache<?, ?> cache;

   public SizeCommand(DataContainer container, Cache<?, ?> cache, Set<Flag> flags) {
      setFlags(flags);
      this.cache = cache;
      this.container = container;
   }

   @Override
   public Object acceptVisitor(InvocationContext ctx, Visitor visitor) throws Throwable {
      return visitor.visitSizeCommand(ctx, this);
   }

   @Override
   public Integer perform(InvocationContext ctx) throws Throwable {
      String useClusterSize = System.getProperty("infinispan.accurate.bulk.ops");
      if (useClusterSize == null || !useClusterSize.equalsIgnoreCase("true")) {
         return localOnlyMethod(ctx);
      } else {
         return iteratorMethod(ctx);
      }
   }

   private int localOnlyMethod(InvocationContext ctx) {
      if (ctx.getLookedUpEntries().isEmpty()) {
         return container.size();
      }

      int size = container.size();
      for (CacheEntry e: ctx.getLookedUpEntries().values()) {
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

   public int iteratorMethod(InvocationContext ctx) {
      int size = 0;
      Map<Object, CacheEntry> contextEntries = ctx.getLookedUpEntries();
      // Keeps track of keys that were found in the context, which means to not count them later
      Set<Object> keys = new HashSet<Object>();
      CloseableIterable<CacheEntry> iterator = cache.getAdvancedCache().withFlags(
            flags != null ? flags.toArray(new Flag[flags.size()]) : null).filterEntries(AcceptAllKeyValueFilter.getInstance()).converter(
            NullValueConverter.getInstance());
      try {
         for (CacheEntry entry : iterator) {
            CacheEntry value = contextEntries.get(entry.getKey());
            if (value != null) {
               keys.add(entry.getKey());
               if (!value.isRemoved()) {
                  size++;
               }
            } else {
               size++;
            }
         }
      } finally {
         try {
            iterator.close();
         } catch (IOException e) {
            // Ignore
         }
      }

      // We can only add context entries if we didn't see it in iterator and it isn't removed
      for (Map.Entry<Object, CacheEntry> entry : contextEntries.entrySet()) {
         if (!keys.contains(entry.getKey()) && !entry.getValue().isRemoved()) {
            size++;
         }
      }

      return size;
   }

   @Override
   public String toString() {
      return "SizeCommand{" +
            "containerSize=" + container.size() +
            '}';
   }
}
