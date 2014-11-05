package org.infinispan.server.hotrod.util;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.infinispan.AdvancedCache;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.distexec.mapreduce.Collator;
import org.infinispan.distexec.mapreduce.MapReduceTask;
import org.infinispan.security.AuthorizationManager;
import org.infinispan.security.Security;
import org.infinispan.security.actions.ExecuteTaskAction;
import org.infinispan.security.actions.GetCacheAuthorizationManagerAction;
import org.infinispan.security.actions.GetCacheConfigurationAction;
import org.infinispan.security.actions.NewMapReduceTaskAction;

/**
 * SecurityActions for the org.infinispan.server.hotrod.util package.
 *
 * Do not move. Do not change class and method visibility to avoid being called from other
 * {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 *
 * @author Tristan Tarrant
 * @since 7.0
 */
final class SecurityActions {
   private static <T> T doPrivileged(PrivilegedAction<T> action) {
      if (System.getSecurityManager() != null) {
         return AccessController.doPrivileged(action);
      } else {
         return Security.doPrivileged(action);
      }
   }

   static Configuration getCacheConfiguration(final AdvancedCache<?, ?> cache) {
      GetCacheConfigurationAction action = new GetCacheConfigurationAction(cache);
      return doPrivileged(action);
   }

   static MapReduceTask newMapReduceTask(AdvancedCache<?, ?> cache) {
      NewMapReduceTaskAction action = new NewMapReduceTaskAction(cache);
      return doPrivileged(action);
   }

   static <T> T executeTask(MapReduceTask task, Collator<?, ?, T> collator) {
      ExecuteTaskAction action = new ExecuteTaskAction(task, collator);
      return (T) doPrivileged(action);
   }
   
   static AuthorizationManager getCacheAuthorizationManager(final AdvancedCache<?, ?> cache) {
      GetCacheAuthorizationManagerAction action = new GetCacheAuthorizationManagerAction(cache);
      return doPrivileged(action);
   }
}
