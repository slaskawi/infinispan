package org.jboss.as.clustering.infinispan.cs.configuration;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.configuration.cache.*;

import java.util.Properties;

/**
 * Configuration which operates only on class names instead of class objects.
 *
 * @author slaskawi
 * @since 7.2
 */
@BuiltBy(DeployedStoreConfigurationBuilder.class)
public class DeployedStoreConfiguration extends AbstractStoreConfiguration implements WithChildConfiguration {

   private final String customStoreClassName;

   public DeployedStoreConfiguration(String customStoreClassName, boolean purgeOnStartup, boolean fetchPersistentState, boolean ignoreModifications,
                                     AsyncStoreConfiguration async, SingletonStoreConfiguration singletonStore, boolean preload,
                                     boolean shared, Properties properties) {
      super(purgeOnStartup, fetchPersistentState, ignoreModifications, async, singletonStore, preload, shared, properties);
      this.customStoreClassName = customStoreClassName;
   }

   public String customStoreClassName() {
      return customStoreClassName;
   }

   @Override
   public StoreConfiguration getChildConfiguration() {
      Thread.currentThread().getContextClassLoader().loadClass(customStoreClassName);
   }
}
