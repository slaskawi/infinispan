package org.infinispan.persistence.leveldb;

import org.infinispan.commons.test.skip.SkipOnOs;
import org.infinispan.configuration.cache.PersistenceConfigurationBuilder;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfiguration;
import org.infinispan.persistence.leveldb.configuration.LevelDBStoreConfigurationBuilder;
import org.testng.annotations.Test;

@Test(groups = "unit", testName = "persistence.leveldb.JniLevelDBCacheStoreTest")
@SkipOnOs({SkipOnOs.OS.SOLARIS, SkipOnOs.OS.WINDOWS})
public class JniLevelDBCacheStoreTest extends LevelDBStoreTest {

   protected LevelDBStoreConfigurationBuilder createCacheStoreConfig(PersistenceConfigurationBuilder lcb) {
      LevelDBStoreConfigurationBuilder builder = super.createCacheStoreConfig(lcb);
      builder.implementationType(LevelDBStoreConfiguration.ImplementationType.JNI);
      return builder;
   }
}
