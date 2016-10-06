package org.infinispan.health;

import org.infinispan.health.impl.HealthImpl;
import org.infinispan.health.impl.jmx.HealthJMXExposerImpl;
import org.infinispan.health.jmx.HealthJMXExposer;
import org.infinispan.manager.EmbeddedCacheManager;

/**
 * An entry point for creating Health instances.
 */
public class HealthFactory {

    private HealthFactory() {
    }

    /**
     * Creates Health API.
     *
     * @param embeddedCacheManager Cache manager to be monitored.
     * @return Health instance.
     */
    public static Health create(EmbeddedCacheManager embeddedCacheManager) {
        Health health = new HealthImpl(embeddedCacheManager);
        HealthJMXExposer jmxExposer = new HealthJMXExposerImpl(health);
        embeddedCacheManager.getGlobalComponentRegistry().registerComponent(jmxExposer, HealthJMXExposer.class);
        return health;
    }
}
