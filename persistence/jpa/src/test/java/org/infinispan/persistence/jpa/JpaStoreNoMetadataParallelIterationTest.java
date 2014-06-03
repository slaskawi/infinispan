package org.infinispan.persistence.jpa;

import org.testng.annotations.Test;
import org.infinispan.test.fwk.CleanupAfterMethod;

@Test(groups = "functional", testName = "persistence.JpaStoreNoMetadataParallelIterationTest")
@CleanupAfterMethod
public class JpaStoreNoMetadataParallelIterationTest extends JpaStoreParallelIterationTest {
   @Override
   public String getPersistenceUnitName() {
      return "org.infinispan.persistence.jpa.no_metadata";
   }

   @Override
   protected boolean storeMetadata() {
      return false;
   }

   @Override
   protected boolean hasMetadata(int i) {
      return false;
   }
}
