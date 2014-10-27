package org.infinispan.persistence;

import static org.testng.AssertJUnit.assertEquals;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.infinispan.Cache;
import org.testng.annotations.AfterClass;
import org.testng.annotations.BeforeClass;
import org.testng.annotations.Test;

/**
 * ClassLoaderFunctionalBulkTest
 * 
 * @author wburns
 * @since JDG 6.4
 */
@Test
public class ClassLoaderFunctionalBulkTest extends CacheLoaderFunctionalTest {
   @BeforeClass
   public void beforeClass() {
      System.setProperty("infinispan.accurate.bulk.ops", "true");
   }
   
   @AfterClass
   public void afterClass() {
      System.clearProperty("infinispan.accurate.bulk.ops");
   }
   
   public void testValuesForCacheLoader() {
      cache.putIfAbsent("k1", "v1");
      List<String> copy1 = copyValues(cache);
      assertEquals(1, copy1.size());
      assertEquals("v1", copy1.get(0));

      cache.putIfAbsent("k2", "v2");
      List<String> copy2 = copyValues(cache);
      assertEquals(2, copy2.size());
      assertEquals(Arrays.asList("v1", "v2"), copy2);
   }

   private List<String> copyValues(Cache<?, String> cache) {
      return new ArrayList<String>(cache.values());
   }
}
