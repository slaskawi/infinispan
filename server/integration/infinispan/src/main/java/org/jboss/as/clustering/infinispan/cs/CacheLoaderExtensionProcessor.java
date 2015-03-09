package org.jboss.as.clustering.infinispan.cs;

import org.infinispan.persistence.spi.CacheLoader;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

public final class CacheLoaderExtensionProcessor extends AbstractCacheStoreExtensionProcessor<CacheLoader> {

    private static final Logger logger = Logger.getLogger(CacheLoaderExtensionProcessor.class.getPackage().getName());

   public CacheLoaderExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AbstractExtensionManagerService<CacheLoader> createService(String serviceName, CacheLoader instance) {
      return new CacheLoaderService(serviceName, instance);
   }

   @Override
   public Class<CacheLoader> getServiceClass() {
      return CacheLoader.class;
   }

   private static class CacheLoaderService extends AbstractExtensionManagerService<CacheLoader> {
      private CacheLoaderService(String serviceName, CacheLoader cacheLoader) {
         super(serviceName, cacheLoader);
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started CacheLoader service with name = %s", serviceName);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped CacheLoader service with name = %s", serviceName);
      }

      @Override
      public CacheLoader getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "CacheLoader-service";
      }
   }

}
