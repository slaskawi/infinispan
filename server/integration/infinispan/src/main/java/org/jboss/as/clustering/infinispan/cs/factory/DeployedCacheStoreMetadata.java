package org.jboss.as.clustering.infinispan.cs.factory;

import org.infinispan.commons.configuration.BuiltBy;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.configuration.cache.CustomStoreConfigurationBuilder;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.configuration.cache.StoreConfigurationBuilder;
import org.jboss.logging.Logger;

import java.util.ArrayList;
import java.util.List;

/**
 * Metadata for deployed Cache Store
 *
 * @author slaskawi
 * @since 7.2
 */
public class DeployedCacheStoreMetadata {

   private static final Logger logger = Logger.getLogger(DeployedCacheStoreMetadata.class.getPackage().getName());

   private String loaderWriterInstanceName;
   private Object loaderWriterRawInstance;
   private Class<? extends StoreConfiguration> storeConfigurationClass;
   private StoreConfiguration storeConfigurationInstance;
   private Class<? extends StoreConfigurationBuilder> storeBuilderClass;
   private List<DeployedCacheApplicationMetadata> applicationMetadatas = new ArrayList<>();

   public DeployedCacheStoreMetadata(Class<? extends StoreConfiguration> storeConfigurationClass) {
      this.storeConfigurationClass = storeConfigurationClass;
   }

   public static DeployedCacheStoreMetadata fromDeployedStoreConfiguration(Class<? extends StoreConfiguration> storeConfigurationClass) {
      DeployedCacheStoreMetadata ret = new DeployedCacheStoreMetadata(storeConfigurationClass);
      ret.createConfigurationInstance();
      ret.createInstanceData();
      ret.createStoreBuilder();
      return ret;
   }

   private void createStoreBuilder() {
      BuiltBy builtBy = storeConfigurationClass.getAnnotation(BuiltBy.class);
      if(builtBy != null) {
         storeBuilderClass = (Class<? extends StoreConfigurationBuilder>) builtBy.value();
      } else {
         storeBuilderClass = CustomStoreConfigurationBuilder.class;
      }
   }

   private void createConfigurationInstance() {
      try {
         storeConfigurationInstance = storeConfigurationClass.newInstance();
      } catch (Exception e) {
         throw new IllegalArgumentException("Could not instantiate instance of " + storeBuilderClass, e);
      }
   }

   private void createInstanceData() {
      ConfigurationFor configurationFor = storeConfigurationClass.getAnnotation(ConfigurationFor.class);
      if(configurationFor == null || configurationFor.value() == null) {
         throw new IllegalArgumentException("Cache Store's configuration must contain a valid " + ConfigurationFor.class.getSimpleName() + " annotation");
      }
      loaderWriterInstanceName = configurationFor.value().getName();
      try {
         loaderWriterRawInstance = configurationFor.value().newInstance();
      } catch (Exception e) {
         throw new IllegalArgumentException("Could not instantiate instance of " + configurationFor.value(), e);
      }
   }


}
