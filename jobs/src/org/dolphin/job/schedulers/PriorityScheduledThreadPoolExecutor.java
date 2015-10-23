package org.dolphin.job.schedulers;


import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by hanyanan on 2015/10/21.
 */
public class PriorityScheduledThreadPoolExecutor extends PausableThreadPoolExecutor
        implements ScheduledExecutorService {
    private static final String TAG = "PriorityScheduledThreadPoolExecutor";

    /**
     * False if should cancel/suppress periodic tasks on shutdown.
     */
    private volatile boolean continueExistingPeriodicTasksAfterShutdown;

    /**
     * False if should cancel non-periodic tasks on shutdown.
     */
    private volatile boolean executeExistingDelayedTasksAfterShutdown = true;

    /**
     * True if ScheduledFutureTask.cancel should remove from queue
     */
    private volatile boolean removeOnCancel = false;

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
            extends FutureTask<V> implements RunnableScheduledFuture<V> {

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
                time += p; // TODO: 下一次运行时间是否是可以调整为 now + period
//            else
//                time = triggerTime(-p);
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

    /**
     * Returns true if can run a task given current run state
     * and run-after-shutdown parameters.
     *
     * @param periodic true if this task periodic, false if delayed
     */
    boolean canRunInCurrentRunState(boolean periodic) {
        boolean running = periodic ? continueExistingPeriodicTasksAfterShutdown : executeExistingDelayedTasksAfterShutdown;
        return !isShutdown() || running;
    }

    public void reject(RunnableScheduledFuture<?> task) {
        RejectedExecutionHandler handler = getRejectedExecutionHandler();
        if (null != handler) {
            handler.rejectedExecution(task, this);
        }
    }

    /**
     * Main execution method for delayed or periodic tasks.  If pool
     * is shut down, rejects the task. Otherwise adds task to queue
     * and starts a thread, if necessary, to run it.  (We cannot
     * prestart the thread to run the task because the task (probably)
     * shouldn't be run yet,) If the pool is shut down while the task
     * is being added, cancel and remove it if required by state and
     * run-after-shutdown parameters.
     *
     * @param task the task
     */
    protected void delayedExecute(RunnableScheduledFuture<?> task) {
        if (!isShutdown() || canRunInCurrentRunState(task.isPeriodic())) {
            super.getQueue().add(task);
            prestartCoreThread();
        } else {
            reject(task);
        }
    }

    /**
     * Requeues a periodic task unless current run state precludes it.
     * Same idea as delayedExecute except drops task rather than rejecting.
     *
     * @param task the task
     */
    protected void reExecutePeriodic(RunnableScheduledFuture<?> task) {
        if (!isShutdown() || canRunInCurrentRunState(true)) {
            super.getQueue().add(task);
            prestartCoreThread();
        } else {
            reject(task);
        }
    }

    public long triggerTime(long delay, TimeUnit unit) {
        return now() + unit.toNanos(delay);
    }


    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public ScheduledFuture<?> schedule(Runnable command, long delay, TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<?> t = new ScheduledFutureTask<Void>(command, null,
                triggerTime(delay, unit));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     */
    public <V> ScheduledFuture<V> schedule(Callable<V> callable,
                                           long delay,
                                           TimeUnit unit) {
        if (callable == null || unit == null)
            throw new NullPointerException();
        RunnableScheduledFuture<V> t = new ScheduledFutureTask<V>(callable,
                triggerTime(delay, unit));
        delayedExecute(t);
        return t;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleAtFixedRate(Runnable command,
                                                  long initialDelay,
                                                  long period,
                                                  TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (period <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft =
                new ScheduledFutureTask<Void>(command,
                        null,
                        triggerTime(initialDelay, unit),
                        unit.toNanos(period));
        sft.outerTask = sft;
        delayedExecute(sft);
        return sft;
    }

    /**
     * @throws RejectedExecutionException {@inheritDoc}
     * @throws NullPointerException       {@inheritDoc}
     * @throws IllegalArgumentException   {@inheritDoc}
     */
    public ScheduledFuture<?> scheduleWithFixedDelay(Runnable command,
                                                     long initialDelay,
                                                     long delay,
                                                     TimeUnit unit) {
        if (command == null || unit == null)
            throw new NullPointerException();
        if (delay <= 0)
            throw new IllegalArgumentException();
        ScheduledFutureTask<Void> sft =
                new ScheduledFutureTask<Void>(command,
                        null,
                        triggerTime(initialDelay, unit),
                        unit.toNanos(-delay));
        sft.outerTask = sft;
        delayedExecute(sft);
        return sft;
    }

    @Override
    public Future<?> submit(Runnable task) {
        // TODO
        return super.submit(task);
    }

    @Override
    public <T> Future<T> submit(Runnable task, T result) {
        // TODO
        return super.submit(task, result);
    }

    @Override
    public <T> Future<T> submit(Callable<T> task) {
        // TODO
        return super.submit(task);
    }

    /**
     * Attempts to stop all actively executing tasks, halts the
     * processing of waiting tasks, and returns a list of the tasks
     * that were awaiting execution.
     * <p/>
     * <p>This method does not wait for actively executing tasks to
     * terminate.  Use {@link #awaitTermination awaitTermination} to
     * do that.
     * <p/>
     * <p>There are no guarantees beyond best-effort attempts to stop
     * processing actively executing tasks.  This implementation
     * cancels tasks via {@link Thread#interrupt}, so any task that
     * fails to respond to interrupts may never terminate.
     *
     * @return list of tasks that never commenced execution.
     * Each element of this list is a {@link ScheduledFuture},
     * including those tasks submitted using {@code execute},
     * which are for scheduling purposes used as the basis of a
     * zero-delay {@code ScheduledFuture}.
     * @throws SecurityException {@inheritDoc}
     */
    public List<Runnable> shutdownNow() {
        return super.shutdownNow();
    }

    /**
     * Returns the task queue used by this executor.  Each element of
     * this queue is a {@link ScheduledFuture}, including those
     * tasks submitted using {@code execute} which are for scheduling
     * purposes used as the basis of a zero-delay
     * {@code ScheduledFuture}.  Iteration over this queue is
     * <em>not</em> guaranteed to traverse tasks in the order in
     * which they will execute.
     *
     * @return the task queue
     */
    public BlockingQueue<Runnable> getQueue() {
        return super.getQueue();
    }


    public PriorityScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                               long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue);
    }

    public PriorityScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                               long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                               ThreadFactory threadFactory) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory);
    }

    public PriorityScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                               long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                               RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, handler);
    }

    public PriorityScheduledThreadPoolExecutor(int corePoolSize, int maximumPoolSize,
                                               long keepAliveTime, TimeUnit unit, BlockingQueue<Runnable> workQueue,
                                               ThreadFactory threadFactory, RejectedExecutionHandler handler) {
        super(corePoolSize, maximumPoolSize, keepAliveTime, unit, workQueue, threadFactory, handler);
    }
}
