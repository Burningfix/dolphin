package org.dolphin.job.schedulers;

import org.dolphin.job.Job;
import org.dolphin.job.Log;
import org.dolphin.job.Scheduler;
import org.dolphin.job.Subscription;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/13.
 */
public class Schedulers {
    public static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
    public static final Scheduler IO_SCHEDULER = null;
    public static final Scheduler COMPUTATION_SCHEDULER = null;
    public static final Scheduler OBSERVER = null;


    public synchronized static void loadJob(Job job) {
        Subscription subscription = new Subscription();
        Runnable runnable = new Runnable() {
            @Override
            public void run() {

            }
        };

    }


    public synchronized static void loadJob(Job job, long delayTime, TimeUnit unit) {

    }

    public synchronized static void loadJob(Job job, long initialDelay, long period, TimeUnit unit) {

    }

    private static class JobRunnable implements Runnable {
        private final Job job;
        private  JobRunnable(Job job) {
            this.job = job;
        }

        @Override
        public void run() {
            Log.i();
        }
    }
}
