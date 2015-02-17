package org.infinispan.persistence.factory;

import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.commons.configuration.ConfigurationFor;
import org.infinispan.commons.util.Util;
import org.infinispan.configuration.cache.CustomStoreConfiguration;
import org.infinispan.configuration.cache.StoreConfiguration;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

/**
 * Default implementation, which uses Local class loader. No external class loading is allowed.
 *
 * @author Sebastian Laskawiec
 * @since 7.1
 */
public class LocalClassLoaderCacheStoreFactory implements CacheStoreFactory {

   private static final Log log = LogFactory.getLog(LocalClassLoaderCacheStoreFactory.class);

   @Override
   public Object createInstance(StoreConfiguration cfg) {
      Class classBasedOnConfigurationAnnotation = getClassBasedOnConfigurationAnnotation(cfg);
      try {
         //getInstance is heavily used, so refactoring it might be risky. However we can safely catch
         //and ignore the exception. Returning null is perfectly legal here.
         Object instance = Util.getInstance(classBasedOnConfigurationAnnotation);
         if(instance != null) {
            return instance;
         }
      } catch (CacheConfigurationException unableToInstantiate) {
         log.debugv("Could not instantiate class {0} using local classloader", classBasedOnConfigurationAnnotation.getName());
      }
      return null;
   }

   private Class getClassBasedOnConfigurationAnnotation(StoreConfiguration cfg) {
      ConfigurationFor annotation = cfg.getClass().getAnnotation(ConfigurationFor.class);
      Class classAnnotation = null;
      if (annotation == null) {
         if (cfg instanceof CustomStoreConfiguration) {
            classAnnotation = ((CustomStoreConfiguration)cfg).customStoreClass();
         }
      } else {
         classAnnotation = annotation.value();
      }
      if (classAnnotation == null) {
         throw log.loaderConfigurationDoesNotSpecifyLoaderClass(cfg.getClass().getName());
      }
      return classAnnotation;
   }

}
