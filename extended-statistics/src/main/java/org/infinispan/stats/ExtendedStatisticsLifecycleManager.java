package org.infinispan.stats;

import javax.management.MBeanServer;
import javax.management.ObjectName;

import org.infinispan.commons.jmx.JmxUtil;
import org.infinispan.configuration.cache.Configuration;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.configuration.cache.InterceptorConfiguration;
import org.infinispan.configuration.global.GlobalConfiguration;
import org.infinispan.configuration.global.GlobalJmxStatisticsConfiguration;
import org.infinispan.factories.ComponentRegistry;
import org.infinispan.factories.GlobalComponentRegistry;
import org.infinispan.factories.components.ManageableComponentMetadata;
import org.infinispan.interceptors.impl.TxInterceptor;
import org.infinispan.jmx.CacheJmxRegistration;
import org.infinispan.jmx.ComponentsJmxRegistration;
import org.infinispan.jmx.ResourceDMBean;
import org.infinispan.lifecycle.ModuleLifecycle;
import org.infinispan.stats.topK.CacheUsageInterceptor;
import org.infinispan.stats.wrappers.ExtendedStatisticInterceptor;
import org.kohsuke.MetaInfServices;

@MetaInfServices(ModuleLifecycle.class)
public class ExtendedStatisticsLifecycleManager implements ModuleLifecycle {

   @Override
   public void cacheStarting(ComponentRegistry cr, Configuration configuration, String cacheName) {
      System.out.println("==== Adding extended statistics ====");

      ConfigurationBuilder builder = new ConfigurationBuilder().read(configuration);
      ExtendedStatisticInterceptor extendedStatisticInterceptor = new ExtendedStatisticInterceptor();
      builder.customInterceptors().addInterceptor().interceptor(extendedStatisticInterceptor)
            .position(InterceptorConfiguration.Position.FIRST);
      CacheUsageInterceptor cacheUsageInterceptor = new CacheUsageInterceptor();
      builder.customInterceptors().addInterceptor().interceptor(cacheUsageInterceptor)
            .before(TxInterceptor.class);

      configuration.customInterceptors().interceptors(builder.build().customInterceptors().interceptors());

      GlobalJmxStatisticsConfiguration globalCfg = cr.getGlobalComponentRegistry().getGlobalConfiguration().globalJmxStatistics();
      if (globalCfg.enabled()) {
         MBeanServer mbeanServer = JmxUtil.lookupMBeanServer(globalCfg.mbeanServerLookup(), globalCfg.properties());
         String groupName = getGroupName(globalCfg, configuration, cacheName);

         cr.registerComponent(extendedStatisticInterceptor, ExtendedStatisticInterceptor.class);
         cr.registerComponent(cacheUsageInterceptor, CacheUsageInterceptor.class);

         // Pick up metadata from the component metadata repository
         ManageableComponentMetadata extendedStatisticsMetadata = cr.getComponentMetadataRepo().findComponentMetadata(ExtendedStatisticInterceptor.class)
               .toManageableComponentMetadata();
         ManageableComponentMetadata cacheUsageMetadata = cr.getComponentMetadataRepo().findComponentMetadata(CacheUsageInterceptor.class)
               .toManageableComponentMetadata();
         // And use this metadata when registering the transport as a dynamic MBean
         try {
            ResourceDMBean extendedStatisticsMBean = new ResourceDMBean(extendedStatisticInterceptor, extendedStatisticsMetadata);
            ResourceDMBean cacheUsageMBean = new ResourceDMBean(cacheUsageInterceptor, cacheUsageMetadata);
            ObjectName extendedStatisticsObjectName = new ObjectName(String.format("%s:%s,component=ExtendedStatistics", globalCfg.domain(), groupName));
            ObjectName cacheUsageObjectName = new ObjectName(String.format("%s:%s,component=CacheUsage", globalCfg.domain(), groupName));
            JmxUtil.registerMBean(extendedStatisticsMBean, extendedStatisticsObjectName, mbeanServer);
            JmxUtil.registerMBean(cacheUsageMBean, cacheUsageObjectName, mbeanServer);
         } catch (Exception e) {
            System.out.println("==== Extended statistics JMX attach failed");
         }
      }

      System.out.println("==== /Adding extended statistics ====");
   }

   @Override
   public void cacheManagerStarting(GlobalComponentRegistry gcr, GlobalConfiguration globalConfiguration) {
      System.out.println("==== CacheManager starting ====");
   }

   private String getGroupName(GlobalJmxStatisticsConfiguration globalConfiguration, Configuration configuration, String cacheName) {
      return CacheJmxRegistration.CACHE_JMX_GROUP + "," + getCacheJmxName(cacheName, configuration.clustering().cacheModeString())
            + ",manager=" + ObjectName.quote(globalConfiguration.cacheManagerName());
   }

   String getCacheJmxName(String cacheName, String cacheModeString) {
      return ComponentsJmxRegistration.NAME_KEY + "=" + ObjectName.quote(
            cacheName + "(" + cacheModeString.toLowerCase() + ")");
   }
}
