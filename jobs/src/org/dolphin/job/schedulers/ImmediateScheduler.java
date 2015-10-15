package org.dolphin.job.schedulers;

import org.dolphin.job.Scheduler;
import org.dolphin.job.Subscription;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/15.
 */
public class ImmediateScheduler implements Scheduler {
    public final static ImmediateScheduler INSTANCE = new ImmediateScheduler();


    private ImmediateScheduler(){
        // just support instance in current class, just support once.
    }


    @Override
    public Subscription schedule(Runnable runnable) {
        runnable.run();
        return null;
    }

    @Override
    public Subscription schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Subscription schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
