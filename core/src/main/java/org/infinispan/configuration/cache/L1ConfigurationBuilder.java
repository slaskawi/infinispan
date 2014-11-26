package org.infinispan.configuration.cache;

import org.infinispan.commons.configuration.Builder;
import org.infinispan.commons.CacheConfigurationException;
import org.infinispan.util.logging.Log;
import org.infinispan.util.logging.LogFactory;

import java.util.concurrent.TimeUnit;

/**
 * Configures the L1 cache behavior in 'distributed' caches instances. In any other cache modes,
 * this element is ignored.
 */

public class L1ConfigurationBuilder extends AbstractClusteringConfigurationChildBuilder implements Builder<L1Configuration> {

   private static final Log log = LogFactory.getLog(L1ConfigurationBuilder.class);

   private boolean enabled = false;
   private int invalidationThreshold = 0;
   private long lifespan = TimeUnit.MINUTES.toMillis(10);
   private long cleanupTaskFrequency = TimeUnit.MINUTES.toMillis(1);

   L1ConfigurationBuilder(ClusteringConfigurationBuilder builder) {
      super(builder);
   }

   /**
    * <p>
    * Determines whether a multicast or a web of unicasts are used when performing L1 invalidations.
    * </p>
    *
    * <p>
    * By default multicast will be used.
    * </p>
    *
    * <p>
    * If the threshold is set to -1, then unicasts will always be used. If the threshold is set to
    * 0, then multicast will be always be used.
    * </p>
    *
    * @param invalidationThreshold the threshold over which to use a multicast
    *
    */
   public L1ConfigurationBuilder invalidationThreshold(int invalidationThreshold) {
      this.invalidationThreshold = invalidationThreshold;
      return this;
   }

   /**
    * Maximum lifespan of an entry placed in the L1 cache.
    */
   public L1ConfigurationBuilder lifespan(long lifespan) {
      this.lifespan = lifespan;
      return this;
   }

   /**
    * Maximum lifespan of an entry placed in the L1 cache.
    */
   public L1ConfigurationBuilder lifespan(long lifespan, TimeUnit unit) {
      return lifespan(unit.toMillis(lifespan));
   }

   /**
    * How often the L1 requestors map is cleaned up of stale items
    */
   public L1ConfigurationBuilder cleanupTaskFrequency(long frequencyMillis) {
      this.cleanupTaskFrequency = frequencyMillis;
      return this;
   }

   /**
    * How often the L1 requestors map is cleaned up of stale items
    */
   public L1ConfigurationBuilder cleanupTaskFrequency(long frequencyMillis, TimeUnit unit) {
      return cleanupTaskFrequency(unit.toMillis(frequencyMillis));
   }

   /**
    * Entries removed due to a rehash will be moved to L1 rather than being removed altogether.
    */
   public L1ConfigurationBuilder enableOnRehash() {
      log.l1OnRehashDeprecated();
      return this;
   }

   /**
    * Entries removed due to a rehash will be moved to L1 rather than being removed altogether.
    */
   public L1ConfigurationBuilder onRehash(boolean enabled) {
      if (enabled) {
         log.l1OnRehashDeprecated();
      }
      return this;
   }

   /**
    * Entries removed due to a rehash will be removed altogether rather than bring moved to L1.
    */
   public L1ConfigurationBuilder disableOnRehash() {
      return this;
   }

   public L1ConfigurationBuilder l1OnRehash(boolean l1OnRehash) {
      if (l1OnRehash) {
         log.l1OnRehashDeprecated();
      }
      return this;
   }

   public L1ConfigurationBuilder enable() {
      this.enabled = true;
      return this;
   }

   public L1ConfigurationBuilder disable() {
      this.enabled = false;
      return this;
   }

   public L1ConfigurationBuilder enabled(boolean enabled) {
      this.enabled = enabled;
      return this;
   }

   @Override
   public void validate() {
      if (enabled) {
         if (!clustering().cacheMode().isDistributed())
            throw new CacheConfigurationException("Enabling the L1 cache is only supported when using DISTRIBUTED as a cache mode.  Your cache mode is set to " + clustering().cacheMode().friendlyCacheModeString());

         if (lifespan < 1)
            throw new CacheConfigurationException("Using a L1 lifespan of 0 or a negative value is meaningless");

      }
   }

   @Override
   public L1Configuration create() {
      return new L1Configuration(enabled, invalidationThreshold, lifespan, false, cleanupTaskFrequency);
   }

   @Override
   public L1ConfigurationBuilder read(L1Configuration template) {
      enabled = template.enabled();
      invalidationThreshold = template.invalidationThreshold();
      lifespan = template.lifespan();
      cleanupTaskFrequency = template.cleanupTaskFrequency();
      return this;
   }

   @Override
   public String toString() {
      return "L1ConfigurationBuilder{" +
            "enabled=" + enabled +
            ", invalidationThreshold=" + invalidationThreshold +
            ", lifespan=" + lifespan +
            ", cleanupTaskFrequency=" + cleanupTaskFrequency +
            ", onRehash=" + false +
            '}';
   }
}
