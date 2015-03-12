package org.jboss.as.clustering.infinispan.cs.deployment;

import org.infinispan.persistence.spi.CacheWriter;
import org.jboss.logging.Logger;
import org.jboss.msc.service.ServiceName;

public final class CacheWriterExtensionProcessor extends AbstractCacheStoreExtensionProcessor<CacheWriter> {

    private static final Logger logger = Logger.getLogger(CacheWriterExtensionProcessor.class.getPackage().getName());

   public CacheWriterExtensionProcessor(ServiceName extensionManagerServiceName) {
      super(extensionManagerServiceName);
   }

   @Override
   public CacheWriterService createService(String serviceName, CacheWriter instance) {
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
      public CacheWriter getValue() {
         return extension;
      }

      @Override
      public String getServiceTypeName() {
         return "CacheWriter-service";
      }
   }

}
