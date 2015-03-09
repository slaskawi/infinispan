package org.jboss.as.clustering.infinispan.cs;

import org.infinispan.persistence.spi.ExternalStore;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

public final class ExternalStoreExtensionProcessor extends AbstractCacheStoreExtensionProcessor<ExternalStore> {

   private static final Logger logger = Logger.getLogger(ExternalStoreExtensionProcessor.class.getPackage().getName());

   public ExternalStoreExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AbstractExtensionManagerService<ExternalStore> createService(String serviceName, ExternalStore instance) {
      return new ExternalStoreService(serviceName, instance);
   }

   @Override
   public Class<ExternalStore> getServiceClass() {
      return ExternalStore.class;
   }

   private static class ExternalStoreService extends AbstractExtensionManagerService<ExternalStore> {
      private ExternalStoreService(String serviceName, ExternalStore ExternalStore) {
         super(serviceName, ExternalStore);
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started ExternalStore service with name = %s", serviceName);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped ExternalStore service with name = %s", serviceName);
      }

      @Override
      public ExternalStore getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "ExternalStore-service";
      }
   }

}
