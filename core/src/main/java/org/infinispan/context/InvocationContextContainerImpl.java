package org.infinispan.context;

import org.infinispan.commons.equivalence.Equivalence;
import org.infinispan.configuration.cache.ClusterLoaderConfiguration;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.Configurations;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.factories.annotations.Inject;
import org.infinispan.factories.annotations.Stop;

import java.util.List;


/**
 * InvocationContextContainer implementation.
 *
 * @author Dan Berindei
 * @since 7.0
 * @private
 */
public class InvocationContextContainerImpl implements InvocationContextContainer {

   // We need to keep the InvocationContext in a thread-local in order to support
   // AdvancedCache.with(ClassLoader). The alternative would be to change the marshalling
   // SPI to accept a ClassLoader parameter.
   private final ThreadLocal<InvocationContext> ctxHolder = new ThreadLocal<InvocationContext>();

   private ClassLoader configuredClassLoader;

   @Inject
   public void init(Configuration configuration, GlobalConfiguration globalConfiguration) {
      configuredClassLoader = Configurations.getClassLoader(configuration, globalConfiguration);
   }

   // As late as possible
   @Stop(priority = 999)
   public void stop() {
      // Because some thread-locals may keep a reference to the InvocationContextContainer,
      // we need to clear the reference to the classloader on stop
      configuredClassLoader = null;
   }

   @Override
   public InvocationContext getInvocationContext(boolean quiet) {
      InvocationContext ctx = ctxHolder.get();
      if (ctx == null && !quiet) throw new IllegalStateException("No InvocationContext associated with current thread!");
      return ctx;
   }

   @Override
   public void setThreadLocal(InvocationContext context) {
      if (isThreadLocalRequired(context)) {
         ctxHolder.set(context);
      }
   }

   @Override
   public void clearThreadLocal() {
      ctxHolder.remove();
   }

   private boolean isThreadLocalRequired(InvocationContext context) {
      return context.getClassLoader() != null &&
            context.getClassLoader() != configuredClassLoader;
   }
}
