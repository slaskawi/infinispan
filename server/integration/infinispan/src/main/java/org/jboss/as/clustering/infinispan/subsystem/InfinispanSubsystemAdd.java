/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2011, Red Hat, Inc., and individual contributors
 * as indicated by the @author tags. See the copyright.txt file in the
 * distribution for a full listing of individual contributors.
 *
 * This is free software; you can redistribute it and/or modify it
 * under the terms of the GNU Lesser General Public License as
 * published by the Free Software Foundation; either version 2.1 of
 * the License, or (at your option) any later version.
 *
 * This software is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the GNU
 * Lesser General Public License for more details.
 *
 * You should have received a copy of the GNU Lesser General Public
 * License along with this software; if not, write to the Free
 * Software Foundation, Inc., 51 Franklin St, Fifth Floor, Boston, MA
 * 02110-1301 USA, or see the FSF site: http://www.fsf.org.
 */

package org.jboss.as.clustering.infinispan.subsystem;

import org.jboss.as.clustering.infinispan.cs.*;
import org.jboss.as.controller.AbstractAddStepHandler;
import org.jboss.as.controller.OperationContext;
import org.jboss.as.controller.OperationFailedException;
import org.jboss.as.controller.ServiceVerificationHandler;
import org.jboss.as.controller.descriptions.ModelDescriptionConstants;
import org.jboss.as.controller.operations.common.Util;
import org.jboss.as.server.AbstractDeploymentChainStep;
import org.jboss.as.server.DeploymentProcessorTarget;
import org.jboss.as.server.deployment.Phase;
import org.jboss.dmr.ModelNode;
import org.jboss.msc.service.ServiceController;
import org.jboss.msc.service.ServiceName;

import java.util.List;

import static org.jboss.as.clustering.infinispan.InfinispanLogger.ROOT_LOGGER;

/**
 * @author Paul Ferraro
 */
public class InfinispanSubsystemAdd extends AbstractAddStepHandler {

    public static final InfinispanSubsystemAdd INSTANCE = new InfinispanSubsystemAdd();

    static ModelNode createOperation(ModelNode address, ModelNode existing) throws OperationFailedException {
        ModelNode operation = Util.getEmptyOperation(ModelDescriptionConstants.ADD, address);
        populate(existing, operation);
        return operation;
    }

    private static void populate(ModelNode source, ModelNode target) throws OperationFailedException {
        target.get(ModelKeys.CACHE_CONTAINER).setEmptyObject();
    }

    protected void populateModel(ModelNode operation, ModelNode model) throws OperationFailedException {
        populate(operation, model);
    }

    protected void performRuntime(OperationContext context, ModelNode operation, ModelNode model, ServiceVerificationHandler verificationHandler, List<ServiceController<?>> newControllers) throws OperationFailedException {
        ROOT_LOGGER.activatingSubsystem();
       final ServiceName serviceName = ServiceName.JBOSS.append("deployable-cache-stores");
       context.addStep(new AbstractDeploymentChainStep() {
          protected void execute(DeploymentProcessorTarget processorTarget) {
             int basePriority = 0x1701;
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new AdvancedCacheLoaderExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new AdvancedCacheWriterExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new AdvancedLoadWriteStoreExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new CacheLoaderExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new CacheWriterExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.INSTALL, ++basePriority, new ExternalStoreExtensionProcessor(serviceName));
             processorTarget.addDeploymentProcessor("infinispan", Phase.DEPENDENCIES, ++basePriority, new ServerExtensionDependenciesProcessor());
          }
       }, OperationContext.Stage.RUNTIME);
    }

    protected boolean requiresRuntimeVerification() {
        return false;
    }
}
