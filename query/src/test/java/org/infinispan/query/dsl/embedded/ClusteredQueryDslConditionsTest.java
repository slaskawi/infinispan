package org.infinispan.query.dsl.embedded;

import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.indexes.impl.IndexManagerHolder;
import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.embedded.sample_domain_model.Account;
import org.infinispan.query.dsl.embedded.sample_domain_model.Address;
import org.infinispan.query.dsl.embedded.sample_domain_model.Transaction;
import org.infinispan.query.dsl.embedded.sample_domain_model.User;
import org.testng.annotations.Test;

import static org.junit.Assert.*;

/**
 * Verifies the functionality of Query DSL in clustered environment for ISPN directory provider.
 *
 * @author anistor@redhat.com
 * @author Anna Manukyan
 * @since 6.0
 */
@Test(groups = "functional", testName = "query.dsl.embedded.ClusteredQueryDslConditionsTest")
public class ClusteredQueryDslConditionsTest extends QueryDslConditionsTest {

   protected static final String TEST_CACHE_NAME = "custom";

   protected Cache<Object, Object> cache1, cache2;

   @Override
   protected Cache<Object, Object> getCacheForWrite() {
      return cache1;
   }

   @Override
   protected Cache<Object, Object> getCacheForQuery() {
      return cache2;
   }

   @Override
   protected void createCacheManagers() throws Throwable {
      ConfigurationBuilder defaultConfiguration = getDefaultClusteredCacheConfig(CacheMode.REPL_SYNC, false);
      defaultConfiguration.clustering()
            .stateTransfer().fetchInMemoryState(true);
      createClusteredCaches(2, defaultConfiguration);

      ConfigurationBuilder cfg = initialCacheConfiguration();
      cfg.clustering()
            .stateTransfer().fetchInMemoryState(true)
            .indexing()
            .enable()
            .indexLocalOnly(true)
            .addProperty("default.indexmanager", "org.infinispan.query.indexmanager.InfinispanIndexManager")
            .addProperty("lucene_version", "LUCENE_36");

      manager(0).defineConfiguration(TEST_CACHE_NAME, cfg.build());
      manager(1).defineConfiguration(TEST_CACHE_NAME, cfg.build());
      cache1 = manager(0).getCache(TEST_CACHE_NAME);
      cache2 = manager(1).getCache(TEST_CACHE_NAME);
   }

   protected ConfigurationBuilder initialCacheConfiguration() {
      return getDefaultClusteredCacheConfig(CacheMode.REPL_SYNC, false);
   }

   @Override
   public void testIndexPresence() {
      checkIndexPresence(cache1);
      checkIndexPresence(cache2);
   }

   private void checkIndexPresence(Cache cache) {
      SearchFactoryImplementor searchFactory = (SearchFactoryImplementor) Search.getSearchManager(cache).getSearchFactory();
      IndexManagerHolder indexManagerHolder = searchFactory.getIndexManagerHolder();

      assertTrue(searchFactory.getIndexedTypes().contains(User.class));
      assertNotNull(indexManagerHolder.getIndexManager(User.class.getName()));

      assertTrue(searchFactory.getIndexedTypes().contains(Account.class));
      assertNotNull(indexManagerHolder.getIndexManager(Account.class.getName()));

      assertTrue(searchFactory.getIndexedTypes().contains(Transaction.class));
      assertNotNull(indexManagerHolder.getIndexManager(Transaction.class.getName()));

      assertFalse(searchFactory.getIndexedTypes().contains(Address.class));
      assertNull(indexManagerHolder.getIndexManager(Address.class.getName()));
   }
}
