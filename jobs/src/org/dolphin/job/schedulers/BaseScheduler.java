package org.dolphin.job.schedulers;

import org.dolphin.job.Log;

import java.util.Timer;
import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/14.
 */
public abstract class BaseScheduler implements Scheduler {

    /**
     * Return work executor of current scheduler.
     * */
    public abstract ScheduledExecutorService getWorkExecutor();

    @Override
    public Subscription schedule(Runnable runnable) {
        ExecutorService executor = getWorkExecutor();
        final Future future = executor.submit(runnable);
        return new FutureSubscription(future);
    }

    @Override
    public Subscription schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.schedule(runnable, delayTime, unit);
        return new FutureSubscription(future);
    }

    @Override
    public Subscription schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
        return new FutureSubscription(future);
    }

    private static class FutureSubscription implements Subscription {
        private final Future future;

        private FutureSubscription(Future future) {
            this.future = future;
        }

        @Override
        public void unsubscription() {
            if(future.isDone() || future.isCancelled()) {
                Log.w("","");
            }else{
                future.cancel(true);
            }
        }

        @Override
        public boolean isUnsubscription() {
            return future.isCancelled();
        }
    }
}
