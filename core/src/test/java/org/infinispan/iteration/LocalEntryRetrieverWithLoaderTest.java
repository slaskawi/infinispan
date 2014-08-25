package org.infinispan.iteration;

import static org.testng.Assert.assertEquals;

import java.util.EnumSet;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Map;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.TimeoutException;

import org.infinispan.Cache;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.context.Flag;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.marshall.TestObjectStreamMarshaller;
import org.infinispan.marshall.core.MarshalledEntryImpl;
import org.infinispan.persistence.dummy.DummyInMemoryStore;
import org.infinispan.persistence.dummy.DummyInMemoryStoreConfigurationBuilder;
import org.infinispan.persistence.manager.PersistenceManager;
import org.infinispan.test.SingleCacheManagerTest;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.TransactionMode;
import org.testng.annotations.Test;

/**
 * Test to verify local entry behavior when a loader is present
 *
 * @author afield
 * @since 7.0
 */
@Test(groups = "functional", testName = "iteration.LocalEntryRetrieverWithLoaderTest")
public class LocalEntryRetrieverWithLoaderTest extends SingleCacheManagerTest {
   protected final static String CACHE_NAME = "LocalEntryRetrieverWithLoaderTest";
   protected ConfigurationBuilder builderUsed;
   protected final boolean tx = false;
   protected final CacheMode cacheMode = CacheMode.LOCAL;

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      builderUsed = new ConfigurationBuilder();
      builderUsed.clustering().cacheMode(cacheMode);
      builderUsed.clustering().hash().numOwners(1);
      builderUsed.persistence().passivation(false).addStore(DummyInMemoryStoreConfigurationBuilder.class)
            .storeName(CACHE_NAME);
      if (tx) {
         builderUsed.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      }
      return TestCacheManagerFactory.createCacheManager(builderUsed);
   }

   private Map<Integer, String> insertDefaultValues(boolean includeLoaderEntry) {
      Cache<Integer, String> cache0 = cache(CACHE_NAME);

      Map<Integer, String> originalValues = new HashMap<Integer, String>();
      originalValues.put(1, "value0");
      originalValues.put(2, "value1");
      originalValues.put(3, "value2");

      cache0.putAll(originalValues);

      PersistenceManager persistenceManager = TestingUtil.extractComponent(cache0, PersistenceManager.class);
      DummyInMemoryStore store = persistenceManager.getStores(DummyInMemoryStore.class).iterator().next();

      TestObjectStreamMarshaller sm = new TestObjectStreamMarshaller();
      try {
         Integer loaderKey = 4;
         String loaderValue = "loader-value";
         store.write(new MarshalledEntryImpl(loaderKey, loaderValue, null, sm));
         if (includeLoaderEntry) {
            originalValues.put(loaderKey, loaderValue);
         }
      } finally {
         sm.stop();
      }
      return originalValues;
   }

   @Test
   public void testCacheLoader() throws InterruptedException, ExecutionException, TimeoutException {
      Map<Integer, String> originalValues = insertDefaultValues(true);

      EntryRetriever<Integer, String> retriever = cache(CACHE_NAME).getAdvancedCache().getComponentRegistry()
            .getComponent(EntryRetriever.class);

      Iterator<CacheEntry> iterator = retriever.retrieveEntries(null, null, null, null);

      // we need this count since the map will replace same key'd value
      int count = 0;
      Map<Object, Object> results = new HashMap<Object, Object>();
      while (iterator.hasNext()) {
         CacheEntry entry = iterator.next();
         results.put(entry.getKey(), entry.getValue());
         count++;
      }
      assertEquals(count, 4);
      assertEquals(originalValues, results);
   }

   @Test
   public void testCacheLoaderIgnored() throws InterruptedException, ExecutionException, TimeoutException {
      Map<Integer, String> originalValues = insertDefaultValues(false);

      EntryRetriever<Integer, String> retriever = cache(CACHE_NAME).getAdvancedCache().getComponentRegistry()
            .getComponent(EntryRetriever.class);

      Iterator<CacheEntry> iterator = retriever.retrieveEntries(null, null,
            EnumSet.of(Flag.SKIP_CACHE_LOAD), null);

      // we need this count since the map will replace same key'd value
      int count = 0;
      Map<Object, Object> results = new HashMap<Object, Object>();
      while (iterator.hasNext()) {
         CacheEntry entry = iterator.next();
         results.put(entry.getKey(), entry.getValue());
         count++;
      }
      assertEquals(count, 3);
      assertEquals(originalValues, results);
   }

}
