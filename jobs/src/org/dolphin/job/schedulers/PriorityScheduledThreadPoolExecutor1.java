package org.dolphin.job.schedulers;

import org.jetbrains.annotations.NotNull;

import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hanyanan on 2015/10/21.
 */
public class PriorityScheduledThreadPoolExecutor1 extends PausableThreadPoolExecutor
        implements ScheduledExecutorService {
    private static final String TAG = "PriorityScheduledThreadPoolExecutor";

    /**
     * Sequence number to break scheduling ties, and in turn to
     * guarantee FIFO order among tied entries.
     */
    private static final AtomicLong sequencer = new AtomicLong(0);

    /**
     * Returns current nanosecond time.
     */
    final long now() {
        return System.nanoTime();
    }

    private class ScheduledFutureTask<V>
            extends FutureTask<V> implements RunnableScheduledFuture<V>, Comparable {

        /**
         * Sequence number to break ties FIFO
         */
        private final long sequenceNumber;

        /**
         * The time the task is enabled to execute in nanoTime units
         */
        private long time;

        /**
         * Period in nanoseconds for repeating tasks.  A positive
         * value indicates fixed-rate execution.  A negative value
         * indicates fixed-delay execution.  A value of 0 indicates a
         * non-repeating task.
         */
        private final long period;

        /**
         * The actual task to be re-enqueued by reExecutePeriodic
         */
        RunnableScheduledFuture<V> outerTask = this;

        /**
         * Index into delay queue, to support faster cancellation.
         */
        int heapIndex;


        private Object input;

        /**
         * Creates a one-shot action with given nanoTime-based trigger time.
         */
        ScheduledFutureTask(Runnable r, V result, long ns) {
            super(r, result);
            this.time = ns;
            this.period = 0;
            this.input = r;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * Creates a periodic action with given nano time and period.
         */
        ScheduledFutureTask(Runnable r, V result, long ns, long period) {
            super(r, result);
            this.time = ns;
            this.period = period;
            this.input = r;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        /**
         * Creates a one-shot action with given nanoTime-based trigger.
         */
        ScheduledFutureTask(Callable<V> callable, long ns) {
            super(callable);
            this.time = ns;
            this.period = 0;
            this.input = callable;
            this.sequenceNumber = sequencer.getAndIncrement();
        }

        public long getDelay(TimeUnit unit) {
            return unit.convert(time - now(), TimeUnit.NANOSECONDS);
        }


        /**
         * 1. 先比对运行时间；
         * 2. 在比较输入
         * 3. 最后比较序列号
         *
         * @param other
         * @return
         */
        public int compareTo(Delayed other) {
            if (other == this) // compare zero ONLY if same object
                return 0;
            // 比较运行时间
            long diff = getDelay(TimeUnit.NANOSECONDS) - other.getDelay(TimeUnit.NANOSECONDS);
            if (diff < 0)
                return -1;
            else if (diff > 0)
                return 1;


            if (other instanceof ScheduledFutureTask) {
                // 对输入进行比较
                ScheduledFutureTask<?> x = (ScheduledFutureTask<?>) other;
                Object in1 = input;
                Object in2 = x.input;
                if (Comparable.class.isInstance(in1) && Comparable.class.isInstance(in2)) {
                    int res = ((Comparable) in1).compareTo(in2);
                    if (res != 0) {
                        return res;
                    }
                }

                // 最后比较序列号
                if (sequenceNumber < x.sequenceNumber)
                    return -1;
                else
                    return 1;
            }

            return 0;
        }

        /**
         * Returns true if this is a periodic (not a one-shot) action.
         *
         * @return true if periodic
         */
        public boolean isPeriodic() {
            return period != 0;
        }

        /**
         * Sets the next time to run for a periodic task.
         */
        private void setNextRunTime() {
            long p = period;
            if (p > 0)
                time += p;
            else
                time = triggerTime(-p);
        }

        public boolean cancel(boolean mayInterruptIfRunning) {
            boolean cancelled = super.cancel(mayInterruptIfRunning);
            if (cancelled && removeOnCancel && heapIndex >= 0)
                remove(this);
            return cancelled;
        }

        /**
         * Overrides FutureTask version so as to reset/requeue if periodic.
         */
        public void run() {
            boolean periodic = isPeriodic();
            if (!canRunInCurrentRunState(periodic))
                cancel(false);
            else if (!periodic)
                ScheduledFutureTask.super.run();
            else if (ScheduledFutureTask.super.runAndReset()) {
                setNextRunTime();
                reExecutePeriodic(outerTask);
            }
        }
    }


    public PriorityScheduledThreadPoolExecutor1(int corePoolSize, int maximumPoolSize,
                                                long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public PriorityScheduledThreadPoolExecutor1(int corePoolSize, int maximumPoolSize,
                                                long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public PriorityScheduledThreadPoolExecutor1(int corePoolSize, int maximumPoolSize,
                                                long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public PriorityScheduledThreadPoolExecutor1(int corePoolSize, int maximumPoolSize,
                                                long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                                ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }

    @NotNull
    @Override
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        return null;
    }

    @NotNull
    @Override
    public <V> ScheduledFuture<V> schedule(Callable<V> callable, long delay, TimeUnit unit) {
        return null;
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command, long initialDelay, long period, TimeUnit unit) {
        return null;
    }

    @NotNull
    @Override
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command, long initialDelay, long delay, TimeUnit unit) {
        return null;
    }
}
