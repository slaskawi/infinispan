package org.infinispan.security.actions;

import java.security.PrivilegedAction;

import org.infinispan.distexec.mapreduce.Collator;
import org.infinispan.distexec.mapreduce.MapReduceTask;

/**
 * ExecuteMapReduceTaskAction.
 * @author Tristan Tarrant
 */
public class ExecuteMapReduceTaskAction<KIn, VIn, KOut, VOut, R> implements PrivilegedAction<R> {
   final MapReduceTask<KIn, VIn, KOut, VOut> task;
   final Collator<KOut, VOut, R> collator;
   
   public ExecuteMapReduceTaskAction(MapReduceTask<KIn, VIn, KOut, VOut> task, Collator<KOut, VOut, R> collator) {
      this.task = task;
      this.collator = collator;
   }

   @Override
   public R run() {
      return task.execute(collator);
   }

}
