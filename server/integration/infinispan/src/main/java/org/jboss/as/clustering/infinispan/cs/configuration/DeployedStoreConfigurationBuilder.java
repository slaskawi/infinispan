package org.jboss.as.clustering.infinispan.cs.configuration;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.configuration.cache.AbstractStoreConfigurationBuilder;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

/**
 * StoreConfigurationBuilder used for stores/loaders that don't have a configuration builder
 *
 * @author slaskawi
 * @since 7.2
 */
public class DeployedStoreConfigurationBuilder extends AbstractStoreConfigurationBuilder<DeployedStoreConfiguration, DeployedStoreConfigurationBuilder> {

   private String customStoreClassName;

   public DeployedStoreConfigurationBuilder(PersistenceConfigurationBuilder builder) {
      super(builder);
   }

   @Override
   public DeployedStoreConfiguration create() {
      return new DeployedStoreConfiguration(customStoreClassName, purgeOnStartup, fetchPersistentState, ignoreModifications,
              async.create(), singletonStore.create(), preload, shared, properties);
   }

   @Override
   public Builder<?> read(DeployedStoreConfiguration template) {
      super.read(template);
      customStoreClassName = template.customStoreClassName();
      return this;
   }

   @Override
   public DeployedStoreConfigurationBuilder self() {
      return this;
   }

   public DeployedStoreConfigurationBuilder customStoreClassName(String customStoreClassName) {
      this.customStoreClassName = customStoreClassName;
      return this;
   }
}
