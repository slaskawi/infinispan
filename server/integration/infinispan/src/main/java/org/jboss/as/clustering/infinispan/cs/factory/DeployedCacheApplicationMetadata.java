package org.jboss.as.clustering.infinispan.cs.factory;

import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;

/**
 * Indicated where the Deployed Cache Store should be applied.
 *
 * @author slaskawi
 * @since 7.2
 */
public class DeployedCacheApplicationMetadata {

   String cacheName;
   PersistenceConfigurationBuilder persistenceConfigurationBuilder;

   public String getCacheName() {
      return cacheName;
   }

   public void setCacheName(String cacheName) {
      this.cacheName = cacheName;
   }

   public PersistenceConfigurationBuilder getPersistenceConfigurationBuilder() {
      return persistenceConfigurationBuilder;
   }

   public void setPersistenceConfigurationBuilder(PersistenceConfigurationBuilder persistenceConfigurationBuilder) {
      this.persistenceConfigurationBuilder = persistenceConfigurationBuilder;
   }
}
