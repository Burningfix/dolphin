package org.dolphin.job.schedulers;

import org.dolphin.job.Log;

import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Executor;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;
import java.util.concurrent.atomic.AtomicBoolean;

/**
 * Created by hanyanan on 2015/10/28.
 */
public class ExecutorScheduler implements Scheduler {
    private static final String TAG = "ExecutorScheduler";
    private final Timer timer = new Timer();
    private final Executor executor;

    public ExecutorScheduler(Executor executor) {
        this.executor = executor;
    }

    @Override
    public void pause() {

    }

    @Override
    public void resume() {

    }

    @Override
    public Future schedule(final Runnable runnable) {
        FutureRunnable futureRunnable = new FutureRunnable(runnable);
        executor.execute(futureRunnable);
        return futureRunnable;
    }

    @Override
    public Future schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        FutureRunnable futureRunnable = new FutureRunnable(runnable);
        SubscriptionTimeTask subscriptionTimeTask = new SubscriptionTimeTask(futureRunnable);
        timer.schedule(subscriptionTimeTask, unit.toMillis(delayTime));
        return futureRunnable;
    }

    @Override
    public Future schedulePeriodically(final Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        FutureRunnable futureRunnable = new FutureRunnable(runnable);
        SubscriptionTimeTask subscriptionTimeTask = new SubscriptionTimeTask(futureRunnable);
        timer.schedule(subscriptionTimeTask, unit.toMillis(initialDelay), unit.toMillis(period));
        return futureRunnable;
    }


    private class SubscriptionTimeTask extends TimerTask {
        private final FutureRunnable runnable;

        private SubscriptionTimeTask(FutureRunnable runnable) {
            this.runnable = runnable;
        }

        @Override
        public void run() {
            runnable.run();
        }
    }


    private static class FutureRunnable implements Future, Runnable {
        protected final AtomicBoolean cancelled = new AtomicBoolean(false);
        protected final AtomicBoolean isDone = new AtomicBoolean(false);
        protected final Runnable realLoadedRunnable;

        public FutureRunnable(Runnable runnable) {
            this.realLoadedRunnable = runnable;
        }

        @Override
        public boolean cancel(boolean mayInterruptIfRunning) {
            if (isDone.get()) {
                Log.w(TAG, "try cancel a finished job!");
                return false;
            }
            if (cancelled.get()) {
                Log.w(TAG, "try cancel a cancelled job!");
            }
            cancelled.set(true);
            return true;
        }

        @Override
        public boolean isCancelled() {
            return cancelled.get();
        }

        @Override
        public boolean isDone() {
            return isDone.get();
        }

        @Override
        public Object get() throws InterruptedException, ExecutionException {
            throw new RuntimeException("Not support get function!");
        }

        @Override
        public Object get(long timeout, TimeUnit unit) throws InterruptedException, ExecutionException, TimeoutException {
            throw new RuntimeException("Not support get function!");
        }

        @Override
        public void run() {
            if (cancelled.get()) {
                return;
            }
            realLoadedRunnable.run();
            isDone.set(true);
        }
    }
}
