package org.dolphin.job.schedulers;

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
    public Future schedule(Runnable runnable) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.schedule(runnable, 0, TimeUnit.MILLISECONDS);
        return future;
    }

    @Override
    public Future schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.schedule(runnable, delayTime, unit);
        return future;
    }

    @Override
    public Future schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        ScheduledExecutorService executor = getWorkExecutor();
        final Future future = executor.scheduleAtFixedRate(runnable, initialDelay, period, unit);
        return future;
    }
}
