package org.dolphin.job.schedulers;

import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/15.
 */
public class ImmediateScheduler implements Scheduler {
    public static ImmediateScheduler sInstance = null;

    private ImmediateScheduler() {
        // just support instance in current class, just support once.
    }

    public synchronized static ImmediateScheduler instance() {
        if (sInstance == null) {
            sInstance = new ImmediateScheduler();
        }
        return sInstance;
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("ImmediateScheduler not support pause function!");
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException("ImmediateScheduler not support resume function!");
    }

    @Override
    public Future schedule(Runnable runnable) {
        runnable.run();
        return null;
    }

    @Override
    public Future schedule(Runnable runnable, long delayTime, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Future schedulePeriodically(Runnable runnable, long initialDelay, long period, TimeUnit unit) {
        throw new UnsupportedOperationException();
    }
}
