package org.infinispan.security.actions;

import java.security.PrivilegedAction;

import org.infinispan.distexec.mapreduce.Collator;
import org.infinispan.distexec.mapreduce.MapReduceTask;

public class ExecuteTaskAction<T> implements PrivilegedAction<T> {
   private final MapReduceTask task;
   private final Collator<?, ?, T> collator;

   public ExecuteTaskAction(MapReduceTask task, Collator collator) {
      this.task = task;
      this.collator = collator;
   }

   @Override
   public T run() {
      return (T)task.execute(collator);
   }

}
