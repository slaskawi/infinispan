package org.infinispan.objectfilter.impl;

import com.google.protobuf.Descriptors;
import org.infinispan.objectfilter.impl.hql.FilterProcessingChain;
import org.infinispan.objectfilter.impl.hql.ProtobufEntityNamesResolver;
import org.infinispan.objectfilter.impl.hql.ProtobufPropertyHelper;
import org.infinispan.objectfilter.impl.predicateindex.ProtobufMatcherEvalContext;
import org.infinispan.protostream.SerializationContext;
import org.infinispan.protostream.WrappedMessage;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * @author anistor@redhat.com
 * @since 7.0
 */
public final class ProtobufMatcher extends BaseMatcher<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> {

   private final SerializationContext serializationContext;

   private final ProtobufEntityNamesResolver entityNamesResolver;

   private final ProtobufPropertyHelper propertyHelper;

   private final Descriptors.Descriptor wrappedMessageDescriptor;

   public ProtobufMatcher(SerializationContext serializationContext) {
      this.serializationContext = serializationContext;
      wrappedMessageDescriptor = serializationContext.getMessageDescriptor(WrappedMessage.PROTOBUF_TYPE_NAME);
      entityNamesResolver = new ProtobufEntityNamesResolver(serializationContext);
      propertyHelper = new ProtobufPropertyHelper(entityNamesResolver, serializationContext);
   }

   @Override
   protected ProtobufMatcherEvalContext startContext(Object instance) {
      ProtobufMatcherEvalContext context = createContext(instance);
      if (context.getEntityType() != null) {
         FilterRegistry<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> filterRegistry = getFilterRegistryForType(context.getEntityType());
         if (filterRegistry != null) {
            context.initMultiFilterContext(filterRegistry);
            return context;
         }
      }
      return null;
   }

   @Override
   protected ProtobufMatcherEvalContext startContext(Object instance, FilterSubscriptionImpl<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> filterSubscription) {
      ProtobufMatcherEvalContext ctx = createContext(instance);
      return ctx.getEntityType() != null && ctx.getEntityType().getFullName().equals(filterSubscription.getEntityTypeName()) ? ctx : null;
   }

   @Override
   protected ProtobufMatcherEvalContext createContext(Object instance) {
      ProtobufMatcherEvalContext ctx = new ProtobufMatcherEvalContext(instance, wrappedMessageDescriptor, serializationContext);
      ctx.unwrapPayload();
      return ctx;
   }

   @Override
   protected FilterProcessingChain<Descriptors.Descriptor> createFilterProcessingChain(Map<String, Object> namedParameters) {
      return FilterProcessingChain.build(entityNamesResolver, propertyHelper, namedParameters);
   }

   @Override
   protected FilterRegistry<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> getFilterRegistryForType(Descriptors.Descriptor entityType) {
      return filtersByTypeName.get(entityType.getFullName());
   }

   @Override
   protected MetadataAdapter<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> createMetadataAdapter(Descriptors.Descriptor messageDescriptor) {
      return new MetadataAdapterImpl(messageDescriptor);
   }

   private static class MetadataAdapterImpl implements MetadataAdapter<Descriptors.Descriptor, Descriptors.FieldDescriptor, Integer> {

      private final Descriptors.Descriptor messageDescriptor;

      MetadataAdapterImpl(Descriptors.Descriptor messageDescriptor) {
         this.messageDescriptor = messageDescriptor;
      }

      @Override
      public String getTypeName() {
         return messageDescriptor.getFullName();
      }

      @Override
      public Descriptors.Descriptor getTypeMetadata() {
         return messageDescriptor;
      }

      @Override
      public List<Integer> translatePropertyPath(List<String> path) {
         List<Integer> propPath = new ArrayList<Integer>(path.size());
         Descriptors.Descriptor md = messageDescriptor;
         for (String prop : path) {
            Descriptors.FieldDescriptor fd = md.findFieldByName(prop);
            propPath.add(fd.getNumber());
            if (fd.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               md = fd.getMessageType();
            } else {
               md = null; // iteration is expected to stop here
            }
         }
         return propPath;
      }

      @Override
      public boolean isRepeatedProperty(List<String> propertyPath) {
         Descriptors.Descriptor md = messageDescriptor;
         for (String prop : propertyPath) {
            Descriptors.FieldDescriptor fd = md.findFieldByName(prop);
            if (fd.isRepeated()) {
               return true;
            }
            if (fd.getJavaType() == Descriptors.FieldDescriptor.JavaType.MESSAGE) {
               md = fd.getMessageType();
            } else {
               md = null; // iteration is expected to stop here
            }
         }
         return false;
      }

      @Override
      public Descriptors.FieldDescriptor makeChildAttributeMetadata(Descriptors.FieldDescriptor parentAttributeMetadata, Integer attribute) {
         return parentAttributeMetadata == null ?
               messageDescriptor.findFieldByNumber(attribute) : parentAttributeMetadata.getMessageType().findFieldByNumber(attribute);
      }

      @Override
      public boolean isComparableProperty(Descriptors.FieldDescriptor attributeMetadata) {
         switch (attributeMetadata.getJavaType()) {
            case INT:
            case LONG:
            case FLOAT:
            case DOUBLE:
            case BOOLEAN:
            case STRING:
            case BYTE_STRING:
            case ENUM:
               return true;
         }
         return false;
      }
   }
}
