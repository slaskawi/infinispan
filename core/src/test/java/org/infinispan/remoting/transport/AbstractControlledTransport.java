package org.infinispan.remoting.transport;

import org.infinispan.commands.ReplicableCommand;
import org.infinispan.remoting.responses.Response;
import org.infinispan.remoting.rpc.ResponseFilter;
import org.infinispan.remoting.rpc.ResponseMode;
import org.infinispan.xsite.XSiteBackup;
import org.infinispan.xsite.XSiteReplicateCommand;

import java.util.Collection;
import java.util.Map;

/**
 * // TODO: Document this
 *
 * @author Pedro Ruivo
 * @since 6.2
 */
public class AbstractControlledTransport extends AbstractDelegatingTransport {

   protected AbstractControlledTransport(Transport actual) {
      super(actual);
   }

   @Override
   public Map<Address, Response> invokeRemotely(Collection<Address> recipients, ReplicableCommand rpcCommand, ResponseMode mode, long timeout, boolean usePriorityQueue, ResponseFilter responseFilter, boolean totalOrder, boolean anycast) throws Exception {
      beforeInvokeRemotely(rpcCommand);
      Map<Address, Response> result = actual.invokeRemotely(recipients, rpcCommand, mode, timeout, usePriorityQueue, responseFilter, totalOrder, anycast);
      return afterInvokeRemotely(rpcCommand, result);
   }

   @Override
   public BackupResponse backupRemotely(Collection<XSiteBackup> backups, XSiteReplicateCommand rpcCommand) throws Exception {
      beforeBackupRemotely(rpcCommand);
      BackupResponse response = actual.backupRemotely(backups, rpcCommand);
      return afterBackupRemotely(rpcCommand, response);
   }

   /**
    * method invoked before a remote invocation.
    *
    * @param command the command to be invoked remotely
    */
   protected void beforeInvokeRemotely(ReplicableCommand command) {
      //no-op by default
   }

   /**
    * method invoked after a successful remote invocation.
    *
    * @param command     the command invoked remotely.
    * @param responseMap can be null if not response is expected.
    * @return the new response map
    */
   protected Map<Address, Response> afterInvokeRemotely(ReplicableCommand command, Map<Address, Response> responseMap) {
      return responseMap;
   }

   /**
    * method invoked before a backup remote invocation.
    *
    * @param command the command to be invoked remotely
    */
   protected void beforeBackupRemotely(XSiteReplicateCommand command) {
      //no-op by default
   }

   /**
    * method invoked after a successful backup remote invocation.
    *
    * @param command  the command invoked remotely.
    * @param response can be null if not response is expected.
    * @return the new response map
    */
   protected BackupResponse afterBackupRemotely(ReplicableCommand command, BackupResponse response) {
      return response;
   }
}
