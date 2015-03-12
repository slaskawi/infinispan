package org.jboss.as.clustering.infinispan.cs.deployment;

import org.infinispan.persistence.spi.*;
import org.jboss.as.server.deployment.*;
import org.jboss.as.server.deployment.module.ModuleDependency;
import org.jboss.as.server.deployment.module.ModuleSpecification;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleIdentifier;
import org.jboss.modules.ModuleLoader;

public class ServerExtensionDependenciesProcessor implements DeploymentUnitProcessor {

   private static final ModuleIdentifier API = ModuleIdentifier.create("org.infinispan");

   @Override
   public void deploy(DeploymentPhaseContext ctx) throws DeploymentUnitProcessingException {
      if (hasInfinispanExtensions(ctx)) {
         DeploymentUnit deploymentUnit = ctx.getDeploymentUnit();
         ModuleSpecification moduleSpec = deploymentUnit.getAttachment(Attachments.MODULE_SPECIFICATION);
         ModuleLoader moduleLoader = Module.getBootModuleLoader();
         moduleSpec.addSystemDependency(new ModuleDependency(moduleLoader, API, false, false, false, false));
      }
   }

   private boolean hasInfinispanExtensions(DeploymentPhaseContext ctx) {
      DeploymentUnit deploymentUnit = ctx.getDeploymentUnit();
      ServicesAttachment sa = deploymentUnit.getAttachment(Attachments.SERVICES);
      if (sa != null) {
         return hasDeployableCache(sa);
      }
      return false;
   }

   @Override
   public void undeploy(DeploymentUnit context) {
      // No-op
   }


   private boolean hasDeployableCache(ServicesAttachment sa) {
      return hasAdvancedCacheLoaders(sa) || hasAdvancedCacheWriters(sa) || hasAdvancedLoadWriteStores(sa) ||
              hasCacheLoader(sa) || hasCacheWriter(sa) || hasExternalStores(sa);
   }

   private boolean hasAdvancedCacheLoaders(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, AdvancedCacheLoader.class);
   }

   private boolean hasAdvancedCacheWriters(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, AdvancedCacheWriter.class);
   }

   private boolean hasAdvancedLoadWriteStores(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, AdvancedLoadWriteStore.class);
   }

   private boolean hasCacheLoader(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, CacheLoader.class);
   }

   private boolean hasCacheWriter(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, CacheWriter.class);
   }

   private boolean hasExternalStores(ServicesAttachment servicesAttachment) {
      return hasExtension(servicesAttachment, ExternalStore.class);
   }

   private boolean hasExtension(ServicesAttachment servicesAttachment, Class<?> extensionClass) {
      return !servicesAttachment.getServiceImplementations(extensionClass.getName()).isEmpty();
   }

}
