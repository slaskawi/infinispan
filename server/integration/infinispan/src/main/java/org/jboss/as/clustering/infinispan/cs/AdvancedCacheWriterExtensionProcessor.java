package org.jboss.as.clustering.infinispan.cs;

import org.infinispan.persistence.spi.AdvancedCacheWriter;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

public final class AdvancedCacheWriterExtensionProcessor extends AbstractCacheStoreExtensionProcessor<AdvancedCacheWriter> {

    private static final Logger logger = Logger.getLogger(AdvancedCacheWriterExtensionProcessor.class.getPackage().getName());

   public AdvancedCacheWriterExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AbstractExtensionManagerService<AdvancedCacheWriter> createService(String serviceName, AdvancedCacheWriter instance) {
      return new AdvancedCacheWriterService(serviceName, instance);
   }

   @Override
   public Class<AdvancedCacheWriter> getServiceClass() {
      return AdvancedCacheWriter.class;
   }

   private static class AdvancedCacheWriterService extends AbstractExtensionManagerService<AdvancedCacheWriter> {
      private AdvancedCacheWriterService(String serviceName, AdvancedCacheWriter AdvancedCacheWriter) {
         super(serviceName, AdvancedCacheWriter);
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started AdvancedCacheWriter service with name = %s", serviceName);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped AdvancedCacheWriter service with name = %s", serviceName);
      }

      @Override
      public AdvancedCacheWriter getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "AdvancedCacheWriter-service";
      }
   }

}
