package org.infinispan.configuration.cache;

/**
 * Marker interface for internal use only.
 *
 * Indicates that given Persistence configuration has a
 *
 * @author slaskawi
 * @since 7.2
 */
public interface WithChildConfiguration<T extends StoreConfiguration> {

   T getChildConfiguration();

}
