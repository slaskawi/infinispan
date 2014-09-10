package org.infinispan.query.dsl.embedded;

import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.test.TestingUtil;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.testng.annotations.AfterClass;
import org.testng.annotations.Test;

import java.io.File;

import static org.junit.Assert.assertTrue;

/**
 * Verifies the functionality of DSL iterators for Filesystem directory provider.
 *
 * @author Anna Manukyan
 * @author anistor@redhat.com
 * @since 6.0
 */
@Test(groups = "functional", testName = "query.dsl.embedded.FilesystemQueryDslIterationTest")
public class FilesystemQueryDslIterationTest extends QueryDslIterationTest {

   private final String indexDirectory = TestingUtil.tmpDirectory(getClass());

   @Override
   protected void createCacheManagers() throws Throwable {
      TestingUtil.recursiveFileRemove(indexDirectory);
      boolean created = new File(indexDirectory).mkdirs();
      assertTrue(created);

      ConfigurationBuilder cfg = TestCacheManagerFactory.getDefaultCacheConfiguration(true);
      cfg.indexing().enable()
            .addProperty("default.directory_provider", "filesystem")
            .addProperty("default.indexBase", indexDirectory)
            .addProperty("lucene_version", "LUCENE_CURRENT");
      createClusteredCaches(1, cfg);
   }

   @AfterClass
   @Override
   protected void destroy() {
      try {
         //first stop cache managers, then clear the index
         super.destroy();
      } finally {
         //delete the index otherwise it will mess up the index for next tests
         TestingUtil.recursiveFileRemove(indexDirectory);
      }
   }
}
