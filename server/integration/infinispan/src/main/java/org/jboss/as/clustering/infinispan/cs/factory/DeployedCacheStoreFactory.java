package org.jboss.as.clustering.infinispan.cs.factory;

import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.persistence.factory.CacheStoreFactory;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;
import org.jboss.as.clustering.infinispan.cs.configuration.DeployedStoreConfiguration;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * Cache Store factory designed for deployed instances.
 *
 * @author Sebastian Laskawiec
 * @since 7.2
 */
public class DeployedCacheStoreFactory implements CacheStoreFactory {

   private static final Log log = LogFactory.getLog(DeployedCacheStoreFactory.class);

   private Map<String, Object> instances = Collections.synchronizedMap(new HashMap<String, Object>());

   @Override
   public Object createInstance(StoreConfiguration cfg) {
      log.error("0");
      if(cfg instanceof DeployedStoreConfiguration) {
         log.error("1");
         DeployedStoreConfiguration deployedConfiguration = (DeployedStoreConfiguration) cfg;
         log.info("Initializing Cache Store: " + deployedConfiguration.customStoreClassName());
         return instances.get(deployedConfiguration.customStoreClassName());
      }
      return null;
   }

   /**
    * Adds deployed instance of a Cache Store.
    *
    * @param className Name of the deployed class. Use <code>myObject.getClass().getName();</code>
    * @param instance Instance.
    */
   public void addInstance(String className, Object instance) {
      instances.put(className, instance);
   }

   public Object getInstance(String className) {
      return instances.get(className);
   }

   /**
    * Removed deployed instance of a Cache Store.
    *
    * @param className Name of the deployed class.
    */
   public void removeInstance(String className) {
      instances.remove(className);
   }

}
