package org.jboss.as.clustering.infinispan.cs;

import org.infinispan.persistence.spi.AdvancedLoadWriteStore;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

public final class AdvancedLoadWriteStoreExtensionProcessor extends AbstractCacheStoreExtensionProcessor<AdvancedLoadWriteStore> {

    private static final Logger logger = Logger.getLogger(AdvancedLoadWriteStoreExtensionProcessor.class.getPackage().getName());

   public AdvancedLoadWriteStoreExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AbstractExtensionManagerService<AdvancedLoadWriteStore> createService(String serviceName, AdvancedLoadWriteStore instance) {
      return new AdvancedCacheLoaderService(serviceName, instance);
   }

   @Override
   public Class<AdvancedLoadWriteStore> getServiceClass() {
      return AdvancedLoadWriteStore.class;
   }

   private static class AdvancedCacheLoaderService extends AbstractExtensionManagerService<AdvancedLoadWriteStore> {
      private AdvancedCacheLoaderService(String serviceName, AdvancedLoadWriteStore AdvancedLoadWriteStore) {
         super(serviceName, AdvancedLoadWriteStore);
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started AdvancedLoadWriteStore service with name = %s", serviceName);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped AdvancedLoadWriteStore service with name = %s", serviceName);
      }

      @Override
      public AdvancedLoadWriteStore getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "AdvancedLoadWriteStore-service";
      }
   }

}
