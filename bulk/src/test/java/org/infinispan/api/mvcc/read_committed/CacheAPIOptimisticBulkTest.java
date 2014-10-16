package org.infinispan.api.mvcc.read_committed;

import org.infinispan.api.CacheAPIBulkTest;
import org.infinispan.util.concurrent.IsolationLevel;
import org.testng.annotations.Test;

@Test(groups = "functional", testName = "api.mvcc.read_committed.CacheAPIOptimisticBulkTest")
public class CacheAPIOptimisticBulkTest extends CacheAPIBulkTest {
   @Override
   protected IsolationLevel getIsolationLevel() {
      return IsolationLevel.READ_COMMITTED;
   }
}
