package org.infinispan.functional;

import static org.testng.Assert.fail;
import static org.testng.AssertJUnit.assertEquals;
import static org.testng.AssertJUnit.assertNull;
import static org.testng.AssertJUnit.assertTrue;

import java.io.Serializable;
import java.util.Collections;
import java.util.NoSuchElementException;
import java.util.Optional;
import java.util.function.BiConsumer;
import java.util.function.Function;
import java.util.stream.IntStream;

import javax.transaction.Status;
import javax.transaction.TransactionManager;

import org.infinispan.commons.CacheException;
import org.infinispan.commons.api.functional.EntryView;
import org.infinispan.commons.api.functional.FunctionalMap;
import org.infinispan.commons.api.functional.Param;
import org.infinispan.commons.marshall.MarshallableFunctions;
import org.infinispan.functional.impl.ReadOnlyMapImpl;
import org.infinispan.test.TestingUtil;
import org.infinispan.transaction.LockingMode;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.Assert;
import org.testng.annotations.Test;

@Test(groups = "functional")
public class FunctionalTxInMemoryTest extends FunctionalInMemoryTest {
   private static final int NUM_KEYS = 2;
   private static final Integer[] INT_KEYS = IntStream.range(0, NUM_KEYS).mapToObj(Integer::valueOf).toArray(Integer[]::new);
   TransactionManager tm;

   @Override
   public Object[] factory() {
      return new Object[]{
            new FunctionalTxInMemoryTest().transactional(true).lockingMode(LockingMode.OPTIMISTIC).isolationLevel(IsolationLevel.READ_COMMITTED),
            new FunctionalTxInMemoryTest().transactional(true).lockingMode(LockingMode.PESSIMISTIC).isolationLevel(IsolationLevel.READ_COMMITTED),
            new FunctionalTxInMemoryTest().transactional(true).lockingMode(LockingMode.PESSIMISTIC).isolationLevel(IsolationLevel.REPEATABLE_READ),
      };
   }

   @Override
   protected void createCacheManagers() throws Throwable {
      super.createCacheManagers();
      tm = TestingUtil.extractComponentRegistry(cache(0)).getComponent(TransactionManager.class);
   }

   @Test(dataProvider = "owningModeAndReadMethod")
   public void testReadLoads(boolean isOwner, ReadMethod method) throws Exception {
      Object[] keys = getKeys(isOwner, NUM_KEYS);
      for (Object key : keys) {
         cache(0, DIST).put(key, key);
      }
      tm.begin();
      for (Object key : keys) {
         assertEquals(method.action.eval(key, ro, (Serializable & Function<EntryView.ReadEntryView<Object, String>, String>) (e -> {
            assertTrue(e.find().isPresent());
            assertEquals(e.get(), e.key());
            return "OK";
         })), "OK");
      }
      tm.commit();
   }

   @Test(dataProvider = "readMethods")
   public void testReadLoadsLocal(ReadMethod method) throws Exception {
      Integer[] keys = INT_KEYS;
      for (Integer key : keys) {
         cache(0).put(key, key);
      }
      tm.begin();
      for (Integer key : keys) {
         assertEquals(method.action.eval(key, lro, (Serializable & Function<EntryView.ReadEntryView<Integer, String>, String>) (e -> {
            assertTrue(e.find().isPresent());
            assertEquals(e.get(), e.key());
            return "OK";
         })), "OK");
      }
      tm.commit();
   }

   @Test(dataProvider = "owningModeAndReadMethod")
   public void testReadsAfterMods(boolean isOwner, ReadMethod method) throws Exception {
      Object KEY = getKey(isOwner);
      cache(0, DIST).put(KEY, "a");

      tm.begin();
      assertEquals("a", rw.eval(KEY, append("b")).join());
      assertEquals("ab", rw.evalMany(Collections.singleton(KEY), append("c")).findAny().get());
      assertEquals(null, rw.eval("otherKey", append("d")).join());
      assertEquals("abc", method.action.eval(KEY, ro, MarshallableFunctions.returnReadOnlyFindOrNull()));
      tm.commit();
   }

   @Test(dataProvider = "owningModeAndReadWrites")
   public void testReadWriteAfterMods(boolean isOwner, WriteMethod method) throws Exception {
      Object KEY = getKey(isOwner);
      cache(0, DIST).put(KEY, "a");

      tm.begin();
      assertEquals("a", rw.eval(KEY, append("b")).join());
      assertEquals("ab", rw.evalMany(Collections.singleton(KEY), append("c")).findAny().get());
      assertEquals(null, rw.eval("otherKey", append("d")).join());
      assertEquals("abc", method.action.eval(KEY, wo, rw,
            MarshallableFunctions.returnReadOnlyFindOrNull(),
            (BiConsumer<EntryView.WriteEntryView<String>, String> & Serializable) (e, prev) -> {}, getClass()));
      tm.commit();
   }

   public void testNonFunctionalReadsAfterMods() throws Exception {
      Object KEY = getKey(false);
      cache(0, DIST).put(KEY, "a");

      tm.begin();
      assertEquals("a", rw.eval(KEY, append("b")).join());
      assertEquals("ab", cache.get(KEY));
      // try second time to make sure the modification is not applied twice
      assertEquals("ab", cache.get(KEY));
      tm.commit();

      tm.begin();
      assertEquals("ab", rw.evalMany(Collections.singleton(KEY), append("c")).findAny().get());
      assertEquals("abc", cache.put(KEY, "abcd"));
      tm.commit();

      tm.begin();
      assertEquals("abcd", cache.get(KEY));
      tm.commit();

      tm.begin();
      wo.eval(KEY, "x", MarshallableFunctions.setValueConsumer()).join();
      assertEquals("x", cache.putIfAbsent(KEY, "otherValue"));
      tm.commit();

      tm.begin();
      wo.eval(KEY, MarshallableFunctions.removeConsumer());
      assertNull(cache.putIfAbsent(KEY, "y"));
      assertEquals("y", ro.eval(KEY, MarshallableFunctions.returnReadOnlyFindOrNull()).join());
      tm.commit();

      tm.begin();
      assertEquals("y", rw.eval(KEY, "z", MarshallableFunctions.setValueReturnPrevOrNull()).join());
      assertTrue(cache.replace(KEY, "z", "a"));
      tm.commit();

      tm.begin();
      assertEquals("a", rw.eval(KEY, append("b")).join());
      assertEquals("ab", cache.getAll(Collections.singleton(KEY)).get(KEY));
      tm.commit();
   }

   @Test(dataProvider = "owningModeAndReadWrites")
   public void testWriteModsInTxContext(boolean isOwner, WriteMethod method) throws Exception {
      Object KEY = getKey(isOwner);
      cache(0, DIST).put(KEY, "a");

      tm.begin();
      assertEquals("a", cache(0, DIST).put(KEY, "b"));
      // read-write operation should execute locally instead
      assertEquals("b", method.action.eval(KEY, null, rw,
            (Function<EntryView.ReadEntryView<Object, String>, String> & Serializable) EntryView.ReadEntryView::get,
            (BiConsumer<EntryView.WriteEntryView<String>, String> & Serializable) (e, prev) -> e.set(prev + "c"), getClass()));
      // make sure that the operation was executed in context
      assertEquals("bc", ro.eval(KEY, MarshallableFunctions.returnReadOnlyFindOrNull()).join());
      tm.commit();
   }

   private static Function<EntryView.ReadWriteEntryView<Object, String>, String> append(String str) {
      return (Serializable & Function<EntryView.ReadWriteEntryView<Object, String>, String>) ev -> {
         Optional<String> prev = ev.find();
         if (prev.isPresent()) {
            ev.set(prev.get() + str);
            return prev.get();
         } else {
            ev.set(str);
            return null;
         }
      };
   }

   @Test(dataProvider = "owningModeAndReadMethod")
   public void testReadOnMissingValues(boolean isOwner, ReadMethod method) throws Exception {
      testReadOnMissingValue(getKeys(isOwner, NUM_KEYS), ro, method);
   }

   @Test(dataProvider = "readMethods")
   public void testReadOnMissingValuesLocal(ReadMethod method) throws Exception {
      testReadOnMissingValue(INT_KEYS, ReadOnlyMapImpl.create(fmapL1).withParams(Param.FutureMode.COMPLETED), method);
   }

   private <K> void testReadOnMissingValue(K[] keys, FunctionalMap.ReadOnlyMap<K, String> ro, ReadMethod method) throws Exception {
      tm.begin();
      for (K key : keys) {
         Assert.assertEquals(ro.eval(key,
               (Function<EntryView.ReadEntryView<K, String>, Boolean> & Serializable) (view -> view.find().isPresent())).join(), Boolean.FALSE);
      }
      tm.commit();

      tm.begin();
      for (K key : keys) {
         try {
            method.action.eval(key, ro, (Function<EntryView.ReadEntryView<K, String>, Object> & Serializable) EntryView.ReadEntryView::get);
            fail("Should throw CacheException:NoSuchElementException");
         } catch (CacheException e) { // catches RemoteException, too
            // The first exception should cause the whole transaction to fail
            assertEquals(NoSuchElementException.class, e.getCause().getClass());
            assertEquals(Status.STATUS_MARKED_ROLLBACK, tm.getStatus());
            tm.rollback();
            break;
         }
      }
      if (tm.getStatus() == Status.STATUS_ACTIVE) {
         tm.commit();
      }
   }

   private Object[] getKeys(boolean isOwner, int num) {
      return IntStream.iterate(0, i -> i + 1)
            .mapToObj(i -> "key" + i)
            .filter(k -> cache(0, DIST).getAdvancedCache().getDistributionManager().getLocality(k).isLocal() == isOwner)
            .limit(num).toArray(Object[]::new);
   }
}
