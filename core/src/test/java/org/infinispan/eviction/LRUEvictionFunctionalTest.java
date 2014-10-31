package org.infinispan.eviction;

import org.testng.annotations.Test;

@Test(groups = {"functional", "smoke"}, testName = "eviction.LRUEvictionFunctionalTest")
public class LRUEvictionFunctionalTest extends BaseEvictionFunctionalTest {

   protected EvictionStrategy getEvictionStrategy() {
      return EvictionStrategy.LRU;
   }

}