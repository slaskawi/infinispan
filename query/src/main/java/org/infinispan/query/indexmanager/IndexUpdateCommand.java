package org.infinispan.query.indexmanager;

import java.util.ArrayList;
import java.util.List;

import org.hibernate.search.SearchException;
import org.hibernate.search.backend.LuceneWork;
import org.hibernate.search.engine.spi.SearchFactoryImplementor;
import org.hibernate.search.indexes.spi.IndexManager;
import org.infinispan.Cache;
import org.infinispan.commands.ReplicableCommand;
import org.infinispan.commands.remote.BaseRpcCommand;
import org.infinispan.context.InvocationContext;
import org.infinispan.query.Search;
import org.infinispan.query.SearchManager;
import org.infinispan.query.backend.KeyTransformationHandler;
import org.infinispan.query.backend.QueryInterceptor;
import org.infinispan.query.impl.CommandInitializer;
import org.infinispan.query.impl.ComponentRegistryUtils;
import org.infinispan.query.impl.CustomQueryCommand;
import org.infinispan.query.impl.ModuleCommandIds;

/**
 * Custom RPC command containing an index update request for the
 * Master IndexManager of a specific cache & index.
 *
 * @author Sanne Grinovero
 */
public class IndexUpdateCommand extends AbstractUpdateCommand {

   public static final byte COMMAND_ID = ModuleCommandIds.UPDATE_INDEX;

   public IndexUpdateCommand(String cacheName) {
      super(cacheName);
   }

   @Override
   public Object perform(InvocationContext ctx) throws Throwable {
      if (queryInterceptor.isStopping()) {
         throw log.cacheIsStoppingNoCommandAllowed(cacheName);
      }
      IndexManager indexManager = searchFactory.getIndexManagerHolder().getIndexManager(indexName);
      if (indexManager == null) {
         throw new SearchException("Unknown index referenced : " + indexName);
      }
      List<LuceneWork> luceneWorks = indexManager.getSerializer().toLuceneWorks(this.serializedModel);
      List<LuceneWork> workToApply = transformKeysToStrings(luceneWorks);//idInString field is not serialized, we need to extract it from the key object
      indexManager.performOperations(workToApply, null);
      return Boolean.TRUE; //Return value to be ignored
   }

   @Override
   public byte getCommandId() {
      return COMMAND_ID;
   }

}
