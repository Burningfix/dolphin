package org.dolphin.job;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/11/10.
 */
public abstract class JobWorkPolicy {
    public static final JobWorkPolicy DEFAULT_JOB_WORK_POLICY = new JobWorkPolicy(0, -1) {

    };

    private final long delayNanoTime;

    private final long periodicNanoTime;

    JobWorkPolicy(long delayNanoTime, long periodicNanoTime) {
        this.delayNanoTime = delayNanoTime;
        this.periodicNanoTime = periodicNanoTime;
    }

    /**
     * The nano time of periodicity running mode;
     * <p/>
     * positive value means will running periodicity, negative means just running once, not support periodicity running.
     *
     * @return the periodicity nano time,negative value means just running once.
     */
    public long periodicNanoTime() {
        return this.periodicNanoTime;
    }

    /**
     * The delay time of running;
     * Any none-positive value will running immediately.<b>The lower delay time means the higher running priority</b>,
     * If the job has want to running front, then set the lower delay time will increase the job running priority.
     *
     * @return positive value mean will waiting specify time
     */
    public long delayNanoTime() {
        return this.delayNanoTime;
    }


    public static JobWorkPolicy immediately() {
        return new JobWorkPolicy(0, -1) {
        };
    }

    public static JobWorkPolicy delayWorkPolicy(long delayTime, TimeUnit timeUnit) {
        return new JobWorkPolicy(timeUnit.toNanos(delayTime), -1) {
        };
    }

    public static JobWorkPolicy periodicWorkPolicy(long periodicTime, TimeUnit timeUnit) {
        return new JobWorkPolicy(0, timeUnit.toNanos(periodicTime)) {
        };
    }

    public static JobWorkPolicy workPolicy(long delayTime, long periodicTime, TimeUnit timeUnit) {
        return new JobWorkPolicy(timeUnit.toNanos(delayTime), timeUnit.toNanos(periodicTime)) {
        };
    }
}
