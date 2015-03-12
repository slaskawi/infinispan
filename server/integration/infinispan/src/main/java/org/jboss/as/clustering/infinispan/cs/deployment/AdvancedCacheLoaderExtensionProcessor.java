package org.jboss.as.clustering.infinispan.cs.deployment;

import org.infinispan.persistence.spi.AdvancedCacheLoader;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

public final class AdvancedCacheLoaderExtensionProcessor extends AbstractCacheStoreExtensionProcessor<AdvancedCacheLoader> {

    private static final Logger logger = Logger.getLogger(AdvancedCacheLoaderExtensionProcessor.class.getPackage().getName());

   public AdvancedCacheLoaderExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AdvancedCacheLoaderService createService(String serviceName, AdvancedCacheLoader instance) {
      return new AdvancedCacheLoaderService(serviceName, instance);
   }

   @Override
   public Class<AdvancedCacheLoader> getServiceClass() {
      return AdvancedCacheLoader.class;
   }

   private static class AdvancedCacheLoaderService extends AbstractExtensionManagerService<AdvancedCacheLoader> {
      private AdvancedCacheLoaderService(String serviceName, AdvancedCacheLoader AdvancedCacheLoader) {
         super(serviceName, AdvancedCacheLoader);
      }

      @Override
      public AdvancedCacheLoader getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "AdvancedCacheLoader-service";
      }
   }

}
