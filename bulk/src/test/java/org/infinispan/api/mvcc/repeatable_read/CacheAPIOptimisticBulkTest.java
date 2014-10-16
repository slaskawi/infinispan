package org.infinispan.api.mvcc.repeatable_read;

import org.infinispan.api.CacheAPIBulkTest;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "api.mvcc.repeatable_read.CacheAPIOptimisticBulkTest")
public class CacheAPIOptimisticBulkTest extends CacheAPIBulkTest {
   @Override
   protected IsolationLevel getIsolationLevel() {
      return IsolationLevel.REPEATABLE_READ;
   }
}