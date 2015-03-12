package org.jboss.as.clustering.infinispan.cs.deployment;

import org.jboss.as.clustering.infinispan.cs.factory.DeployedCacheStoreFactory;
import org.jboss.as.clustering.infinispan.cs.factory.DeployedCacheStoreFactoryService;
import org.jboss.as.server.deployment.*;
import org.jboss.logging.Logger;
import org.jboss.modules.Module;
import org.jboss.modules.ModuleClassLoader;
import org.jboss.msc.service.*;
import org.jboss.msc.value.InjectedValue;

import java.lang.reflect.Constructor;
import java.util.List;

public abstract class AbstractCacheStoreExtensionProcessor<T> implements DeploymentUnitProcessor {

    private static final Logger logger = Logger.getLogger(AbstractCacheStoreExtensionProcessor.class.getPackage().getName());

   private final ServiceName extensionManagerServiceName;

   protected AbstractCacheStoreExtensionProcessor(ServiceName extensionManagerServiceName) {
      this.extensionManagerServiceName = extensionManagerServiceName;
   }

    @Override
    public void deploy(DeploymentPhaseContext ctx) throws DeploymentUnitProcessingException {
        DeploymentUnit deploymentUnit = ctx.getDeploymentUnit();
        Module module = deploymentUnit.getAttachment(Attachments.MODULE);
        ServicesAttachment servicesAttachment = deploymentUnit.getAttachment(Attachments.SERVICES);
        if (module != null && servicesAttachment != null)
            addServices(ctx, servicesAttachment, module);
    }

   @Override
   public void undeploy(DeploymentUnit deploymentUnit) {
      // Deploy only adds services, so no need to do anything here
      // since these services are automatically removed.
   }

    private void addServices(DeploymentPhaseContext ctx, ServicesAttachment servicesAttachment, Module module) {
        Class<T> serviceClass = getServiceClass();
        List<String> implementationNames = servicesAttachment.getServiceImplementations(serviceClass.getName());
        ModuleClassLoader classLoader = module.getClassLoader();
        for (String serviceClassName : implementationNames) {
            try {
                Class<? extends T> clazz = classLoader.loadClass(serviceClassName).asSubclass(serviceClass);
                Constructor<? extends T> ctor = clazz.getConstructor();
                T instance = ctor.newInstance();
                installService(ctx, serviceClassName, instance);
            } catch (Exception e) {
                throw new IllegalStateException("Could not instantiate class " + serviceClassName, e);
            }
        }
    }

   public final void installService(DeploymentPhaseContext ctx, String implementationClassName, T instance) {
      AbstractExtensionManagerService<T> service = createService(implementationClassName, instance);
      ServiceName extensionServiceName = ServiceName.JBOSS.append(service.getServiceTypeName(), implementationClassName.replaceAll("\\.", "_"));
      logger.error("Registering service: " + extensionServiceName);
      ServiceBuilder<T> serviceBuilder = ctx.getServiceTarget().addService(extensionServiceName, service);
      serviceBuilder.setInitialMode(ServiceController.Mode.ACTIVE);
      serviceBuilder.addDependency(DeployedCacheStoreFactoryService.SERVICE_NAME, DeployedCacheStoreFactory.class, service.getDeployedCacheStoreFactory());
      serviceBuilder.install();
   }

    public abstract Class<T> getServiceClass();

    public abstract AbstractExtensionManagerService<T> createService(String serviceName, T instance);

   protected static abstract class AbstractExtensionManagerService<T> implements Service<T> {

      protected final T extension;
      protected final String className;
      protected InjectedValue<DeployedCacheStoreFactory> deployedCacheStoreFactory = new InjectedValue<>();

      protected AbstractExtensionManagerService(String className, T extension) {
         this.extension = extension;
         this.className = className;
      }

      @Override
      public void start(StartContext context) {
         logger.debugf("Started AdvancedCacheLoader service with name = %s", className + " " + deployedCacheStoreFactory);
         deployedCacheStoreFactory.getValue().addInstance(className, extension);
      }

      @Override
      public void stop(StopContext context) {
         logger.debugf("Stopped AdvancedCacheLoader service with name = %s", className);
         deployedCacheStoreFactory.getValue().removeInstance(className);
      }

      public InjectedValue<DeployedCacheStoreFactory> getDeployedCacheStoreFactory() {
         return deployedCacheStoreFactory;
      }

      public abstract String getServiceTypeName();
   }
}
