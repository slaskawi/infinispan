package org.jboss.as.clustering.infinispan.cs;

import org.infinispan.persistence.spi.CacheWriter;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;
import org.jboss.msc.service.StartContext;
import org.jboss.msc.service.StopContext;

public final class CacheWriterExtensionProcessor extends AbstractCacheStoreExtensionProcessor<CacheWriter> {

    private static final Logger logger = Logger.getLogger(CacheWriterExtensionProcessor.class.getPackage().getName());

   public CacheWriterExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public AbstractExtensionManagerService<CacheWriter> createService(String serviceName, CacheWriter instance) {
      return new CacheWriterService(serviceName, instance);
   }

   @Override
   public Class<CacheWriter> getServiceClass() {
      return CacheWriter.class;
   }

   private static class CacheWriterService extends AbstractExtensionManagerService<CacheWriter> {
      private CacheWriterService(String serviceName, CacheWriter CacheWriter) {
         super(serviceName, CacheWriter);
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started CacheWriter service with name = %s", serviceName);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped CacheWriter service with name = %s", serviceName);
      }

      @Override
      public CacheWriter getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "CacheWriter-service";
      }
   }

}
