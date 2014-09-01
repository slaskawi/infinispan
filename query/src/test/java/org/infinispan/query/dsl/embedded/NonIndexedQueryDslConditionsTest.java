package org.infinispan.query.dsl.embedded;

import org.hibernate.hql.ParsingException;
import org.infinispan.configuration.cache.ConfigurationBuilder;
import org.infinispan.manager.EmbeddedCacheManager;
import org.infinispan.query.Search;
import org.infinispan.query.dsl.Query;
import org.infinispan.query.dsl.QueryFactory;
import org.infinispan.query.dsl.embedded.sample_domain_model.User;
import org.infinispan.test.fwk.TestCacheManagerFactory;
import org.infinispan.transaction.TransactionMode;
import org.testng.annotations.Test;

import java.util.List;
import static org.junit.Assert.assertEquals;

/**
 * Test for query conditions (filtering) on cache without indexing. Exercises the whole query DSL on the sample domain
 * model.
 *
 * @author anistor@redhat.com
 * @since 7.0
 */
@Test(groups = "functional", testName = "query.dsl.NonIndexedQueryDslConditionsTest")
public class NonIndexedQueryDslConditionsTest extends QueryDslConditionsTest {

   @Override
   protected EmbeddedCacheManager createCacheManager() throws Exception {
      ConfigurationBuilder cfg = getDefaultStandaloneCacheConfig(true);
      cfg.transaction().transactionMode(TransactionMode.TRANSACTIONAL);
      return TestCacheManagerFactory.createCacheManager(cfg);
   }

   @Override
   protected QueryFactory getQueryFactory() {
      return Search.getQueryFactory(cache);
   }

   @Test(expectedExceptions = IllegalArgumentException.class, expectedExceptionsMessageRegExp = "Indexing was not enabled on this cache.*")
   @Override
   public void testIndexPresence() {
      Search.getSearchManager(cache).getSearchFactory();
   }

   @Test(expectedExceptions = ParsingException.class, expectedExceptionsMessageRegExp = "ISPN000405:.*")
   @Override
   public void testInvalidEmbeddedAttributeQuery() throws Exception {
      super.testInvalidEmbeddedAttributeQuery();
   }

   /**
    * This test works for non-indexed mode so we re-enable it here.
    */
   @Test
   @Override
   public void testNullOnIntegerField() throws Exception {
      super.testNullOnIntegerField();
   }

   public void testAnd5() throws Exception {
      QueryFactory qf = getQueryFactory();

      Query q =  qf.from(User.class)
            .having("id").lt(1000)
            .and().having("age").lt(1000)
            .toBuilder().build();

      List<User> list = q.list();
      assertEquals(1, list.size());
      assertEquals(1, list.get(0).getId());
   }
}
