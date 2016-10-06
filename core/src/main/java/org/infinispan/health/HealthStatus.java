package org.infinispan.health;

/**
 * General Health status.
 *
 * @author Sebastian Łaskawiec
 * @since 9.0
 */
public enum HealthStatus {
    /**
     * Given entity is unhealthy.
     */
    UNHEALTHY,

    /**
     * Given entity is healthy.
     */
    HEALTHY,

    /**
     * Given entity is healthy but a rebalance is in progress.
     */
    REBALANCING
}
