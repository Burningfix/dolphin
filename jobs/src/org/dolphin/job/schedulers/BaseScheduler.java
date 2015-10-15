package org.dolphin.job.schedulers;

import org.dolphin.job.Scheduler;
import org.dolphin.job.Subscription;

import java.util.Timer;
import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/14.
 */
public class BaseScheduler implements Scheduler {
    public static Timer sTimer = new Timer();

    /**
     * Return work executor of current scheduler.
     * */
    public Executor getWorkExecutor(){
        return null;
    }

    @Override
    public Subscription schedule(Runnable runnable) {
        Executor executor = getWorkExecutor();
        executor.execute(runnable);
        return null;
    }

    @Override
    public Subscription schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        long delayMillTimes = unit.toMillis(delayTime);



        return null;
    }

    @Override
    public Subscription schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        return null;
    }
}
