package org.dolphin.job.schedulers;

import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hanyanan on 2015/10/15.
 */
public class ImmediateScheduler implements Scheduler {
    public final static ImmediateScheduler INSTANCE = new ImmediateScheduler();

    ImmediateScheduler(){
        // just support instance in current class, just support once.
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
