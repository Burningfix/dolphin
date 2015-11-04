package org.dolphin.job.schedulers;

import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/13.
 */
public class Schedulers {
    public static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
    public final Scheduler io;
    public final Scheduler computationScheduler;
    public final Scheduler immediateScheduler;
    public static Scheduler OBSERVER_SCHEDULER = null;

    private static final Schedulers INSTANCE = new Schedulers();

    private Schedulers() {
        computationScheduler = new ComputationScheduler();
        io = new IOScheduler();
        immediateScheduler = ImmediateScheduler.instance();
    }

    /**
     * Creates and returns a {@link Scheduler} that executes work immediately on the current thread.
     *
     * @return an {@link ImmediateScheduler} instance
     */
    public static Scheduler immediate() {
        return ImmediateScheduler.instance();
    }

//    /**
//     * Creates and returns a {@link Scheduler} that queues work on the current thread to be executed after the
//     * current work completes.
//     *
//     * @return a {@link TrampolineScheduler} instance
//     */
//    public static Scheduler trampoline() {
//        return TrampolineScheduler.instance();
//    }

    /**
     * Creates and returns a {@link Scheduler} that creates a new {@link Thread} for each unit of work.
     * <p>
     * Unhandled errors will be delivered to the scheduler Thread's {@link java.lang.Thread.UncaughtExceptionHandler}.
     *
     * @return a {@link NewThreadScheduler} instance
     */
    public static Scheduler newThread() {
        return new NewThreadScheduler();
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
        return INSTANCE.io;
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
