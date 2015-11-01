package org.dolphin.job.schedulers;

import org.dolphin.job.util.Log;

import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/14.
 */
abstract class BaseScheduler implements Scheduler {

    /**
     * Return work executor of current scheduler.
     * */
    public abstract ScheduledExecutorService getWorkExecutor();

    @Override
    public Subscription schedule(Runnable runnable) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.schedule(runnable, 0, TimeUnit.MILLISECONDS);
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
