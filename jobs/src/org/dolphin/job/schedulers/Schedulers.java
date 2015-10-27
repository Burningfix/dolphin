package org.dolphin.job.schedulers;

import org.dolphin.job.*;
import org.dolphin.job.util.Log;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.tuple.*;
import org.dolphin.lib.IOUtil;

import java.io.Closeable;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/13.
 */
public class Schedulers {
    public static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
    public final Scheduler IO_SCHEDULER;
    public final Scheduler COMPUTATION_SCHEDULER;
    public static Scheduler OBSERVER_SCHEDULER = null;
    public final Scheduler newThreadScheduler;
    public static Scheduler ImmediateScheduler = new ImmediateScheduler();

    private static final Schedulers INSTANCE = new Schedulers();

    private Schedulers() {
        COMPUTATION_SCHEDULER = new ComputationScheduler();
        IO_SCHEDULER = new IOScheduler();
    }

    /**
     * Creates and returns a {@link Scheduler} that executes work immediately on the current thread.
     *
     * @return an {@link ImmediateScheduler} instance
     */
    public static Scheduler immediate() {
        return ImmediateScheduler.instance();
    }

    /**
     * Creates and returns a {@link Scheduler} that queues work on the current thread to be executed after the
     * current work completes.
     *
     * @return a {@link TrampolineScheduler} instance
     */
    public static Scheduler trampoline() {
        return TrampolineScheduler.instance();
    }

    /**
     * Creates and returns a {@link Scheduler} that creates a new {@link Thread} for each unit of work.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link NewThreadScheduler} instance
     */
    public static Scheduler newThread() {
        return INSTANCE.newThreadScheduler;
    }

    /**
     * Creates and returns a {@link Scheduler} intended for computational work.
     * <p>
     * This can be used for event-loops, processing callbacks and other computational work.
     * <p>
     * Do not perform IO-bound work on this scheduler. Use {@link #io()} instead.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link Scheduler} meant for computation-bound work
     */
    public static Scheduler computation() {
        return INSTANCE.computationScheduler;
    }

    /**
     * Creates and returns a {@link Scheduler} intended for IO-bound work.
     * <p>
     * The implementation is backed by an {@link Executor} thread-pool that will grow as needed.
     * <p>
     * This can be used for asynchronously performing blocking IO.
     * <p>
     * Do not perform computational work on this scheduler. Use {@link #computation()} instead.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link Scheduler} meant for IO-bound work
     */
    public static Scheduler io() {
        return INSTANCE.ioScheduler;
    }

    /**
     * Creates and returns a {@code TestScheduler}, which is useful for debugging. It allows you to test
     * schedules of events by manually advancing the clock at whatever pace you choose.
     *
     * @return a {@code TestScheduler} meant for debugging
     */
    public static TestScheduler test() {
        return new TestScheduler();
    }

    /**
     * Converts an {@link Executor} into a new Scheduler instance.
     *
     * @param executor
     *          the executor to wrap
     * @return the new Scheduler wrapping the Executor
     */
    public static Scheduler from(Executor executor) {
        return new ExecutorScheduler(executor);
    }
}
