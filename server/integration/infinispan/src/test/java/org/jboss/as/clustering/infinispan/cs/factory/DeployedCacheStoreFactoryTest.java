package org.jboss.as.clustering.infinispan.cs.factory;

import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;

public class DeployedCacheStoreFactoryTest {

   @Test
   public void testAddingInstance() throws Exception {
      //given
      MyCustomStore testedObject = new MyCustomStore();
      DeployedCacheStoreFactory factory = new DeployedCacheStoreFactory();

      //when
      factory.addInstance(MyCustomStore.class.getName(), testedObject);
      Object instance = factory.createInstance(new MyCustomStoreConfiguration());

      //then
      assertEquals(testedObject, instance);
   }

   @Test
   public void testAddingAndRemovingInstance() throws Exception {
      //given
      MyCustomStore testedObject = new MyCustomStore();
      DeployedCacheStoreFactory factory = new DeployedCacheStoreFactory();

      //when
      factory.addInstance(MyCustomStore.class.getName(), testedObject);
      factory.removeInstance(MyCustomStore.class.getName());
      Object instance = factory.createInstance(new MyCustomStoreConfiguration());

      //then
      assertNull(instance);
   }

}