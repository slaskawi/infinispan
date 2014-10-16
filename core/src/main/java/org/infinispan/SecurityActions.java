package org.infinispan;

import java.security.AccessController;
import java.security.PrivilegedAction;

import org.infinispan.security.Security;

/**
 * SecurityActions for the org.infinispan package.
 *
 * Do not move. Do not change class and method visibility to avoid being called from other
 * {@link java.security.CodeSource}s, thus granting privilege escalation to external code.
 *
 * @author William Burns
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

   static String getSystemProperty(final String propertyName) {
      PrivilegedAction<String> action = new PrivilegedAction<String>() {
         @Override
         public String run() {
            return System.getProperty(propertyName);
         }
      };
      return doPrivileged(action);
   }
}
