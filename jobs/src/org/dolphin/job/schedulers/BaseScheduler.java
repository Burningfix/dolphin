package org.dolphin.job.schedulers;

import org.dolphin.job.Log;

import java.util.Timer;
import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/14.
 */
public class BaseScheduler implements Scheduler {

    /**
     * Return work executor of current scheduler.
     * */
    public ScheduledExecutorService getWorkExecutor(){
        return null;
    }

    @Override
    public Subscription schedule(Runnable runnable) {
        ExecutorService executor = getWorkExecutor();
        final Future future = executor.submit(runnable);
        return new FutureSubscription(future);
    }

    @Override
    public Subscription schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        long delayMillTimes = unit.toMillis(delayTime);
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.schedule(runnable, delayTime, unit);
        return new FutureSubscription(future);
    }

    @Override
    public Subscription schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return null;
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
