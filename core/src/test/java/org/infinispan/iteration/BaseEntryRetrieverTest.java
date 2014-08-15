package org.infinispan.iteration;

import org.infinispan.Cache;
import org.infinispan.commons.util.CloseableIterable;
import org.infinispan.commons.util.CloseableIterator;
import org.infinispan.configuration.cache.CacheMode;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.container.entries.CacheEntry;
import org.infinispan.container.entries.TransientMortalCacheEntry;
import org.infinispan.distribution.MagicKey;
import org.infinispan.filter.CollectionKeyFilter;
import org.infinispan.filter.CompositeKeyValueFilterConverter;
import org.infinispan.filter.Converter;
import org.infinispan.filter.KeyFilter;
import org.infinispan.filter.KeyFilterAsKeyValueFilter;
import org.infinispan.filter.KeyValueFilterConverter;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.metadata.Metadata;
import org.infinispan.test.MultipleCacheManagersTest;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.TransactionMode;
import org.testng.annotations.Test;

import java.io.IOException;
import java.io.Serializable;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

import static org.testng.Assert.assertEquals;
import static org.testng.Assert.assertNotNull;

/**
 * Base class for entry retriever tests
 *
 * @author wburns
 * @since 7.0
 */
@Test(groups = "functional", testName = "iteration.BaseEntryRetrieverTest")
public abstract class BaseEntryRetrieverTest extends MultipleCacheManagersTest {
   protected final String CACHE_NAME = getClass().getName();
   protected ConfigurationBuilder builderUsed;
   protected final boolean tx;
   protected final CacheMode cacheMode;

   public BaseEntryRetrieverTest(boolean tx, CacheMode mode) {
      this.tx = tx;
      cacheMode = mode;
   }

   protected abstract Object getKeyTiedToCache(Cache<?, ?> cache);

   @Override
   protected void createCacheManagers() throws Throwable {
      builderUsed = new ConfigurationBuilder();
      builderUsed.clustering().cacheMode(cacheMode);
      if (tx) {
         builderUsed.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      }
      if (cacheMode.isClustered()) {
         builderUsed.clustering().hash().numOwners(2);
         builderUsed.clustering().stateTransfer().chunkSize(50);
         createClusteredCaches(3, CACHE_NAME, builderUsed);
      } else {
         EmbeddedCacheManager cm = TestCacheManagerFactory.createCacheManager(builderUsed);
         cacheManagers.add(cm);
      }
   }

   protected Map<Object, String> putValuesInCache() {
      // This is linked to keep insertion order
      Map<Object, String> valuesInserted = new LinkedHashMap<Object, String>();
      Cache<Object, String> cache = cache(0, CACHE_NAME);
      Object key = getKeyTiedToCache(cache);
      cache.put(key, key.toString());
      valuesInserted.put(key, key.toString());
      return valuesInserted;
   }

   @Test
   public void simpleTest() {
      Map<Object, String> values = putValuesInCache();

      EntryRetriever<MagicKey, String> retriever = cache(0, CACHE_NAME).getAdvancedCache().getComponentRegistry().getComponent(
            EntryRetriever.class);

      CloseableIterator<CacheEntry> iterator = retriever.retrieveEntries(null, null, null, null);
      Map<MagicKey, String> results = mapFromIterator(iterator);
      assertEquals(values, results);
   }

   @Test
   public void simpleTestIteratorWithMetadata() {
      // This is linked to keep insertion order
      Set<CacheEntry> valuesInserted = new HashSet<CacheEntry>();
      Cache<Object, String> cache = cache(0, CACHE_NAME);
      for (int i = 0; i < 3; ++i) {
         Object key = getKeyTiedToCache(cache);
         TimeUnit unit = TimeUnit.MINUTES;
         cache.put(key, key.toString(), 10, unit, i + 1, unit);

         valuesInserted.add(new TransientMortalCacheEntry(key, key.toString(), unit.toMillis(i + 1), unit.toMillis(10),
                                                          System.currentTimeMillis()));
      }

      EntryRetriever<Object, String> retriever = cache(0, CACHE_NAME).getAdvancedCache().getComponentRegistry().getComponent(
            EntryRetriever.class);

      CloseableIterator<CacheEntry> iterator = retriever.retrieveEntries(null, null, null, null);
      Set<CacheEntry> retrievedValues = new HashSet<CacheEntry>();
      while (iterator.hasNext()) {
         CacheEntry entry = iterator.next();
         retrievedValues.add(entry);
      }
      assertEquals(retrievedValues.size(), valuesInserted.size());
      // Have to do our own equals since Transient uses created time which we can't guarantee will equal
      for (CacheEntry inserted : valuesInserted) {
         CacheEntry found = null;
         for (CacheEntry retrieved : retrievedValues) {
            if (retrieved.getKey().equals(inserted.getKey())) {
               found = retrieved;
               break;
            }
         }
         assertNotNull(found, "No retrieved Value matching" + inserted);
         assertEquals(found.getValue(), inserted.getValue());
         assertEquals(found.getMaxIdle(), inserted.getMaxIdle());
         assertEquals(found.getLifespan(), inserted.getLifespan());
      }
   }

   @Test
   public void simpleTestLocalFilter() {
      Map<Object, String> values = putValuesInCache();
      Iterator<Map.Entry<Object, String>> iter = values.entrySet().iterator();
      Map.Entry<Object, String> excludedEntry = iter.next();
      // Remove it so comparison below will be correct
      iter.remove();

      EntryRetriever<MagicKey, String> retriever = cache(0, CACHE_NAME).getAdvancedCache().getComponentRegistry().getComponent(
            EntryRetriever.class);

      CloseableIterator<CacheEntry> iterator = retriever.retrieveEntries(
            new KeyFilterAsKeyValueFilter<MagicKey, String>(new CollectionKeyFilter<Object>(Collections.singleton(excludedEntry.getKey()))),
            null, null, null);
      Map<MagicKey, String> results = mapFromIterator(iterator);
      assertEquals(values, results);
   }

   @Test
   public void testPublicAPI() {
      Map<Object, String> values = putValuesInCache();
      Iterator<Map.Entry<Object, String>> iter = values.entrySet().iterator();
      Map.Entry<Object, String> excludedEntry = iter.next();
      // Remove it so comparison below will be correct
      iter.remove();


      Cache<MagicKey, String> cache = cache(0, CACHE_NAME);
      EntryIterable<MagicKey, String> iterable = cache.getAdvancedCache().filterEntries(
            new KeyFilterAsKeyValueFilter<MagicKey, String>(new CollectionKeyFilter<Object>(
                  Collections.singleton(excludedEntry.getKey()))));

      Map<MagicKey, String> results = mapFromIterable(iterable);
      assertEquals(values, results);
   }

   @Test
   public void testPublicAPIWithConverter() {
      Map<Object, String> values = putValuesInCache();
      Iterator<Map.Entry<Object, String>> iter = values.entrySet().iterator();
      Map.Entry<Object, String> excludedEntry = iter.next();
      // Remove it so comparison below will be correct
      iter.remove();


      Cache<MagicKey, String> cache = cache(0, CACHE_NAME);
      EntryIterable<MagicKey, String> iterable = cache.getAdvancedCache().filterEntries(
            new KeyFilterAsKeyValueFilter<MagicKey, String>(new CollectionKeyFilter<Object>(
                  Collections.singleton(excludedEntry.getKey()))));

      Map<MagicKey, String> results = mapFromIterable(iterable.converter(new StringTruncator(2, 5)));

      assertEquals(values.size(), results.size());
      for (Map.Entry<Object, String> entry : values.entrySet()) {
         assertEquals(entry.getValue().substring(2, 7), results.get(entry.getKey()));
      }
   }

   @Test
   public void testFilterAndConverterCombined() {
      Map<Object, String> values = putValuesInCache();
      Iterator<Map.Entry<Object, String>> iter = values.entrySet().iterator();
      Map.Entry<Object, String> excludedEntry = iter.next();
      // Remove it so comparison below will be correct
      iter.remove();


      Cache<MagicKey, String> cache = cache(0, CACHE_NAME);
      KeyValueFilterConverter<MagicKey, String, String> filterConverter = new CompositeKeyValueFilterConverter<MagicKey, String, String>(
            new KeyFilterAsKeyValueFilter<Object, String>(new CollectionKeyFilter<Object>(Collections.singleton(excludedEntry.getKey()))),
            new StringTruncator(2, 5));
      EntryIterable<MagicKey, String> iterable = cache.getAdvancedCache().filterEntries(filterConverter);
      Map<MagicKey, String> results = mapFromIterable(iterable);

      assertEquals(values.size(), results.size());
      for (Map.Entry<Object, String> entry : values.entrySet()) {
         assertEquals(entry.getValue().substring(2, 7), results.get(entry.getKey()));
      }
   }

   protected static <K, V> Map<K, V> mapFromIterator(Iterator<CacheEntry> iterator) {
      Map<K, V> map = new HashMap<K, V>();
      while (iterator.hasNext()) {
         CacheEntry entry = iterator.next();
         map.put((K) entry.getKey(), (V) entry.getValue());
      }
      return map;
   }

   protected static <K, V> Map<K, V> mapFromIterator(CloseableIterator<CacheEntry> iterator) {
      try {
         return mapFromIterator((Iterator<CacheEntry>)iterator);
      } finally {
         try {
            iterator.close();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   protected static <K, V> Map<K, V> mapFromIterable(Iterable<CacheEntry> iterable) {
      Map<K, V> map = new HashMap<K, V>();
      for (CacheEntry entry : iterable) {
         map.put((K) entry.getKey(), (V) entry.getValue());
      }
      return map;
   }

   protected static <K, V> Map<K, V> mapFromIterable(CloseableIterable<CacheEntry> iterable) {
      try {
         return mapFromIterable((Iterable<CacheEntry>)iterable);
      } finally {
         try {
            iterable.close();
         } catch (IOException e) {
            throw new RuntimeException(e);
         }
      }
   }

   protected static class StringTruncator implements Converter<Object, String, String>, Serializable {
      private final int beginning;
      private final int length;

      public StringTruncator(int beginning, int length) {
         this.beginning = beginning;
         this.length = length;
      }

      @Override
      public String convert(Object key, String value, Metadata metadata) {
         if (value != null && value.length() > beginning + length) {
            return value.substring(beginning, beginning + length);
         } else {
            return value;
         }
      }
   }
}
