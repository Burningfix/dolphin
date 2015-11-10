package org.dolphin.job.schedulers;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanyanan on 2015/10/14.
 */
public class ComputationScheduler extends BaseScheduler implements Scheduler {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ComputationScheduler #" + count.getAndIncrement());
        }
    };

    private static final BlockingQueue sPoolWorkQueue =
            new DelayedPriorityBlockingQueue();

    public static RejectedExecutionHandler sRejectedExecutionHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // TODO: print log
        }
    };

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static final PriorityScheduledThreadPoolExecutor THREAD_POOL_EXECUTOR
            = new PriorityScheduledThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory, sRejectedExecutionHandler);


    @Override
    public ScheduledExecutorService getWorkExecutor() {
        return THREAD_POOL_EXECUTOR;
    }

    @Override
    public void pause() {
        THREAD_POOL_EXECUTOR.pause();
    }

    @Override
    public void resume() {
        THREAD_POOL_EXECUTOR.resume();
    }
}
