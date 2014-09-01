package org.infinispan.objectfilter.impl.predicateindex;

import org.infinispan.objectfilter.impl.util.ComparableComparator;
import org.infinispan.objectfilter.impl.util.IntervalTree;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Holds all predicates that are subscribed for a certain attribute. This class is not thread safe and leaves this
 * responsibility to the caller.
 *
 * @param <AttributeDomain> the type of the values of the attribute
 * @author anistor@redhat.com
 * @since 7.0
 */
final class Predicates<AttributeDomain> {

   /**
    * Holds all subscriptions for a single predicate.
    */
   private static class Subscriptions {

      private final Predicate predicate;

      /**
       * The callbacks of the subscribed predicates.
       */
      private final List<PredicateIndex.Subscription> subscriptions = new ArrayList<PredicateIndex.Subscription>();

      private Subscriptions(Predicate predicate) {
         this.predicate = predicate;
      }

      void add(PredicateIndex.Subscription subscription) {
         subscriptions.add(subscription);
      }

      void remove(PredicateIndex.Subscription subscription) {
         subscriptions.remove(subscription);
      }

      //todo [anistor] this is an improvement but still does not eliminate precessing of attributes that have only suspended subscribers
      boolean isActive(MatcherEvalContext<?> ctx) {
         return ctx.getSuspendedSubscriptionsCounter(predicate) < subscriptions.size();
      }
   }

   // todo [anistor] this cannot be used right now because ProtobufMatcherEvalContext.DUMMY_VALUE is not really comparable
   private final boolean valueIsComparable;

   /**
    * The predicates that have a condition based on an order relation. This allows them to be represented by an interval
    * tree.
    */
   private IntervalTree<AttributeDomain, Subscriptions> orderedPredicates;

   /**
    * The predicates that are based on an arbitrary condition that is not an order relation.
    */
   private Map<Predicate<AttributeDomain>, Subscriptions> unorderedPredicates;

   Predicates(boolean valueIsComparable) {
      this.valueIsComparable = valueIsComparable;
   }

   public void notifyMatchingSubscribers(MatcherEvalContext<?> ctx, AttributeDomain attributeValue) {
      if (attributeValue instanceof Comparable && orderedPredicates != null) {
         for (IntervalTree.Node<AttributeDomain, Subscriptions> n : orderedPredicates.stab(attributeValue)) {
            Subscriptions subscriptions = n.value;
            if (subscriptions.isActive(ctx)) {
               for (PredicateIndex.Subscription s : subscriptions.subscriptions) {
                  //todo [anistor] if we also notify non-matching interval conditions this could lead to faster short-circuiting evaluation
                  s.handleValue(ctx, true);
               }
            }
         }
      }

      if (unorderedPredicates != null) {
         for (Map.Entry<Predicate<AttributeDomain>, Subscriptions> e : unorderedPredicates.entrySet()) {
            Subscriptions subscriptions = e.getValue();
            if (subscriptions.isActive(ctx)) {
               boolean conditionSatisfied = e.getKey().getCondition().match(attributeValue);
               for (PredicateIndex.Subscription s : subscriptions.subscriptions) {
                  s.handleValue(ctx, conditionSatisfied);
               }
            }
         }
      }
   }

   public void addPredicate(PredicateIndex.Subscription subscription) {
      Subscriptions subscriptions;
      Predicate<AttributeDomain> predicate = subscription.getPredicateNode().getPredicate();
      if (predicate.getInterval() != null) {
         if (orderedPredicates == null) {
            // in this case AttributeDomain extends Comparable for sure
            orderedPredicates = new IntervalTree<AttributeDomain, Subscriptions>(new ComparableComparator());
         }
         IntervalTree.Node<AttributeDomain, Subscriptions> n = orderedPredicates.add(predicate.getInterval());
         if (n.value == null) {
            subscriptions = new Subscriptions(predicate);
            n.value = subscriptions;
         } else {
            subscriptions = n.value;
         }
      } else {
         if (unorderedPredicates == null) {
            unorderedPredicates = new HashMap<Predicate<AttributeDomain>, Subscriptions>();
         }
         subscriptions = unorderedPredicates.get(predicate);
         if (subscriptions == null) {
            subscriptions = new Subscriptions(predicate);
            unorderedPredicates.put(predicate, subscriptions);
         }
      }
      subscriptions.add(subscription);
   }

   public void removePredicate(PredicateIndex.Subscription subscription) {
      Predicate<AttributeDomain> predicate = subscription.getPredicateNode().getPredicate();
      if (predicate.getInterval() != null) {
         if (orderedPredicates != null) {
            IntervalTree.Node<AttributeDomain, Subscriptions> n = orderedPredicates.findNode(predicate.getInterval());
            if (n != null) {
               n.value.remove(subscription);
               if (n.value.subscriptions.isEmpty()) {
                  orderedPredicates.remove(n);
               }
            } else {
               throwIllegalStateException();
            }
         } else {
            throwIllegalStateException();
         }
      } else {
         if (unorderedPredicates != null) {
            Subscriptions subscriptions = unorderedPredicates.get(predicate);
            if (subscriptions != null) {
               subscriptions.remove(subscription);
               if (subscriptions.subscriptions.isEmpty()) {
                  unorderedPredicates.remove(predicate);
               }
            } else {
               throwIllegalStateException();
            }
         } else {
            throwIllegalStateException();
         }
      }
   }

   public boolean isEmpty() {
      return (unorderedPredicates == null || unorderedPredicates.isEmpty())
            && (orderedPredicates == null || orderedPredicates.isEmpty());
   }

   private static void throwIllegalStateException() throws IllegalStateException {
      // this is not expected to happen unless a programming error slipped through
      throw new IllegalStateException("Reached an invalid state");
   }
}
