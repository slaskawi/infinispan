package org.infinispan.test;

import java.util.concurrent.TimeUnit;
import java.util.function.Supplier;

/**
 * A generic wait synchronizer for tests.
 *
 * @author slaskawi
 */
public class WaitFor {

    private static final TimeUnit DEFAULT_UNIT = TimeUnit.MILLISECONDS;
    private static final long DEFAULT_TIMEOUT = 100;

    private final Supplier<Boolean> condition;
    private TimeUnit timeUnit = DEFAULT_UNIT;
    private long timeout = DEFAULT_TIMEOUT;

    private WaitFor(Supplier<Boolean> condition) {
        this.condition = condition;
    }

    /**
     * Creates new instance of the waiter with condition.
     *
     * @param condition Condition to be checked during waiting (<code>true</code> means OK to proceed).
     * @return This waiter.
     */
    public static WaitFor condition(Supplier<Boolean> condition) {
        return new WaitFor(condition);
    }

    /**
     * Specifies how long do we want to wait.
     *
     * @param timeout Timeout
     * @param timeUnit Time units used for timeout
     * @return This waiter
     */
    public WaitFor atMost(long timeout, TimeUnit timeUnit) {
        this.timeout = timeout;
        this.timeUnit = timeUnit;
        return this;
    }

    /**
     * Performs synchronous wait.
     * <p>
     *     The implementation uses {@link Thread#yield()} for letting other threads to do progress.
     * </p>
     * @throws AssertionError If timeout was reached and the condition is still false.
     */
    public void await() throws AssertionError {
        long endTime = System.currentTimeMillis() + TimeUnit.MILLISECONDS.convert(timeout, timeUnit);
        while(!condition.get()) {
            if(System.currentTimeMillis() > endTime) {
                throw new AssertionError("Timeout reached and the condition is still false");
            }
            Thread.yield();
        }
    }
}
