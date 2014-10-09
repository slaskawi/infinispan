/*
 * JBoss, Home of Professional Open Source.
 * Copyright 2012, Red Hat, Inc., and individual contributors
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

import org.jboss.as.controller.*;
import org.jboss.as.controller.operations.validation.EnumValidator;
import org.jboss.as.controller.registry.AttributeAccess;
import org.jboss.as.controller.registry.ManagementResourceRegistration;
import org.jboss.as.controller.services.path.ResolvePathHandler;
import org.jboss.dmr.ModelNode;
import org.jboss.dmr.ModelType;

/**
 * Resource description for the addressable resource /subsystem=infinispan/cache-container=X
 *
 * @author Richard Achmatowicz (c) 2011 Red Hat Inc.
 */
public class CacheContainerResource extends SimpleResourceDefinition {

    public static final PathElement CONTAINER_PATH = PathElement.pathElement(ModelKeys.CACHE_CONTAINER);

    // attributes
    static final AttributeDefinition ALIAS =
            new SimpleAttributeDefinitionBuilder(ModelKeys.ALIAS, ModelType.STRING, true)
                    .setXmlName(Attribute.NAME.getLocalName())
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleListAttributeDefinition ALIASES = SimpleListAttributeDefinition.Builder.of(ModelKeys.ALIASES, ALIAS).
            setAllowNull(true).
            build();

    static final SimpleAttributeDefinition CACHE_CONTAINER_MODULE =
            new SimpleAttributeDefinitionBuilder(ModelKeys.MODULE, ModelType.STRING, true)
                    .setXmlName(Attribute.MODULE.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new ModuleIdentifierValidator(true))
                    .setDefaultValue(new ModelNode().set("org.jboss.as.clustering.infinispan"))
                    .build();

    // make default-cache non required (AS7-3488)
    static final SimpleAttributeDefinition DEFAULT_CACHE =
            new SimpleAttributeDefinitionBuilder(ModelKeys.DEFAULT_CACHE, ModelType.STRING, true)
                    .setXmlName(Attribute.DEFAULT_CACHE.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition EVICTION_EXECUTOR =
            new SimpleAttributeDefinitionBuilder(ModelKeys.EVICTION_EXECUTOR, ModelType.STRING, true)
                    .setXmlName(Attribute.EVICTION_EXECUTOR.getLocalName())
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition JNDI_NAME =
            new SimpleAttributeDefinitionBuilder(ModelKeys.JNDI_NAME, ModelType.STRING, true)
                    .setXmlName(Attribute.JNDI_NAME.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition LISTENER_EXECUTOR =
            new SimpleAttributeDefinitionBuilder(ModelKeys.LISTENER_EXECUTOR, ModelType.STRING, true)
                    .setXmlName(Attribute.LISTENER_EXECUTOR.getLocalName())
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition NAME =
            new SimpleAttributeDefinitionBuilder(ModelKeys.NAME, ModelType.STRING, true)
                    .setXmlName(Attribute.NAME.getLocalName())
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition REPLICATION_QUEUE_EXECUTOR =
            new SimpleAttributeDefinitionBuilder(ModelKeys.REPLICATION_QUEUE_EXECUTOR, ModelType.STRING, true)
                    .setXmlName(Attribute.REPLICATION_QUEUE_EXECUTOR.getLocalName())
                    .setAllowExpression(false)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .build();

    static final SimpleAttributeDefinition START =
            new SimpleAttributeDefinitionBuilder(ModelKeys.START, ModelType.STRING, true)
                    .setXmlName(Attribute.START.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_ALL_SERVICES)
                    .setValidator(new EnumValidator<StartMode>(StartMode.class, true, false))
                    .setDefaultValue(new ModelNode().set(StartMode.LAZY.name()))
                    .build();

    static final SimpleAttributeDefinition STATISTICS =
            new SimpleAttributeDefinitionBuilder(ModelKeys.STATISTICS, ModelType.BOOLEAN, true)
                    .setXmlName(Attribute.STATISTICS.getLocalName())
                    .setAllowExpression(true)
                    .setFlags(AttributeAccess.Flag.RESTART_RESOURCE_SERVICES)
                    .setDefaultValue(new ModelNode().set(true))
                    .build()
;


    static final AttributeDefinition[] CACHE_CONTAINER_ATTRIBUTES = {DEFAULT_CACHE, ALIASES, JNDI_NAME, START, LISTENER_EXECUTOR, EVICTION_EXECUTOR, REPLICATION_QUEUE_EXECUTOR, CACHE_CONTAINER_MODULE, STATISTICS};

    // operations
    static final OperationDefinition ALIAS_ADD = new SimpleOperationDefinitionBuilder("add-alias", InfinispanExtension.getResourceDescriptionResolver("cache-container.alias"))
            .setParameters(NAME)
            .build();

    static final OperationDefinition ALIAS_REMOVE = new SimpleOperationDefinitionBuilder("remove-alias", InfinispanExtension.getResourceDescriptionResolver("cache-container.alias"))
            .setParameters(NAME)
            .build();

   static final ListAttributeDefinition PROTO_URLS =
         new StringListAttributeDefinition.Builder("file-urls")
               .build();

   static final ListAttributeDefinition PROTO_NAMES =
           new StringListAttributeDefinition.Builder("file-names")
                   .build();

   static final ListAttributeDefinition PROTO_CONTENTS =
           new StringListAttributeDefinition.Builder("file-contents")
                   .build();

   static final OperationDefinition UPLOAD_PROTO = new SimpleOperationDefinitionBuilder("upload-proto-schemas", InfinispanExtension.getResourceDescriptionResolver("cache-container"))
          .setParameters(PROTO_NAMES, PROTO_URLS)
          .build();

   static final OperationDefinition REGISTER_PROTO = new SimpleOperationDefinitionBuilder("register-proto-schemas", InfinispanExtension.getResourceDescriptionResolver("cache-container"))
           .setParameters(PROTO_NAMES, PROTO_CONTENTS)
           .build();

    static final OperationDefinition CLI_INTERPRETER = new SimpleOperationDefinitionBuilder("cli-interpreter", InfinispanExtension.getResourceDescriptionResolver(ModelKeys.CACHE_CONTAINER))
          .build();

    private final ResolvePathHandler resolvePathHandler;
    private final boolean runtimeRegistration;
    public CacheContainerResource(final ResolvePathHandler resolvePathHandler, boolean runtimeRegistration) {
        super(CONTAINER_PATH,
                InfinispanExtension.getResourceDescriptionResolver(ModelKeys.CACHE_CONTAINER),
                CacheContainerAdd.INSTANCE,
                CacheContainerRemove.INSTANCE);
        this.resolvePathHandler = resolvePathHandler;
        this.runtimeRegistration = runtimeRegistration;
    }

    @Override
    public void registerAttributes(ManagementResourceRegistration resourceRegistration) {
        super.registerAttributes(resourceRegistration);

        // the handlers need to take account of alias
        final OperationStepHandler writeHandler = new CacheContainerWriteAttributeHandler(CACHE_CONTAINER_ATTRIBUTES);
        for (AttributeDefinition attr : CACHE_CONTAINER_ATTRIBUTES) {
            resourceRegistration.registerReadWriteAttribute(attr, CacheContainerReadAttributeHandler.INSTANCE, writeHandler);
        }

        if (runtimeRegistration) {
            // register runtime cache container read-only metrics (attributes and handlers)
            CacheContainerMetricsHandler.INSTANCE.registerMetrics(resourceRegistration);
        }
    }

    @Override
    public void registerOperations(ManagementResourceRegistration resourceRegistration) {
        super.registerOperations(resourceRegistration);
        // register add-alias and remove-alias
        resourceRegistration.registerOperationHandler(CacheContainerResource.ALIAS_ADD, AddAliasCommand.INSTANCE);
        resourceRegistration.registerOperationHandler(CacheContainerResource.ALIAS_REMOVE, RemoveAliasCommand.INSTANCE);
        resourceRegistration.registerOperationHandler(CacheContainerResource.UPLOAD_PROTO, UploadProtoFileOperationHandler.INSTANCE);
        resourceRegistration.registerOperationHandler(CacheContainerResource.REGISTER_PROTO, RegisterProtoSchemasOperationHandler.INSTANCE);
        resourceRegistration.registerOperationHandler(CLI_INTERPRETER, CliInterpreterHandler.INSTANCE);
    }

    @Override
    public void registerChildren(ManagementResourceRegistration resourceRegistration) {
        super.registerChildren(resourceRegistration);

        // child resources
        resourceRegistration.registerSubModel(new TransportResource());
        resourceRegistration.registerSubModel(new CacheContainerSecurityResource());
        resourceRegistration.registerSubModel(new LocalCacheResource(resolvePathHandler, runtimeRegistration));
        resourceRegistration.registerSubModel(new InvalidationCacheResource(resolvePathHandler, runtimeRegistration));
        resourceRegistration.registerSubModel(new ReplicatedCacheResource(resolvePathHandler, runtimeRegistration));
        resourceRegistration.registerSubModel(new DistributedCacheResource(resolvePathHandler, runtimeRegistration));
    }
}
