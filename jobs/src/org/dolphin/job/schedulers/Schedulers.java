package org.dolphin.job.schedulers;

import org.dolphin.job.*;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.tuple.FiveTuple;
import org.dolphin.job.tuple.FourTuple;
import org.dolphin.job.tuple.SixTuple;
import org.dolphin.job.tuple.ThreeTuple;
import org.dolphin.job.tuple.Tuple;
import org.dolphin.job.tuple.Tuples;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.IOUtil;

import java.io.Closeable;
import java.util.Iterator;
import java.util.WeakHashMap;
import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/13.
 */
public class Schedulers {
    public static ScheduledExecutorService scheduledThreadPool = Executors.newScheduledThreadPool(5);
    public static final Scheduler IO_SCHEDULER = null;
    public static final Scheduler COMPUTATION_SCHEDULER = null;
    public static final Scheduler OBSERVER = null;

    private static final WeakHashMap<Object, Subscription> sJobReference = new WeakHashMap<Object, Subscription>();

    public synchronized static void loadJob(final Job job) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedule(jobRunnable);
        sJobReference.put(job, subscription);
    }

    public synchronized static void loadJob(Job job, long delayTime, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedule(jobRunnable, delayTime, unit);
        sJobReference.put(job, subscription);
    }

    public synchronized static void loadJob(Job job, long initialDelay, long period, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedulePeriodically(jobRunnable, initialDelay, period, unit);
        sJobReference.put(job, subscription);
    }

    public synchronized static void abort(Job job) {
        Subscription subscription = sJobReference.get(job);
        if(!subscription.isUnsubscription()) {
            subscription.unsubscription();
        }
    }

    /**
     * Get default work schedule for specify job.
     */
    private static Scheduler getWorkScheduler(final Job job) {
        Scheduler scheduler = job.getWorkScheduler();
        if(null == scheduler) {
            return COMPUTATION_SCHEDULER;
        }
        return scheduler;
    }

    private static class JobRunnable implements Runnable {
        private final Job job;

        private JobRunnable(Job job) {
            this.job = job;
        }

        private Scheduler getObserverScheduler() {
            Scheduler scheduler = job.getObserverScheduler();
            if (null == scheduler) {
                scheduler = ImmediateScheduler.INSTANCE;
            }
            return scheduler;
        }

        private void notifyCancellation() {
            if (job.getObserver() == null) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    job.getObserver().onCancellation(job);
                }
            });
        }

        private void notifyComplete(final Object res) {
            if (job.getObserver() == null) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        job.getObserver().onCompleted(job, res);
                    } else {
                        job.getObserver().onCancellation(job);
                    }
                }
            });
        }

        private void notifyFailed(final Job job, final Throwable error) {
            if (job.getObserver() == null) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        job.getObserver().onFailed(job, error);
                    } else {
                        job.getObserver().onCancellation(job);
                    }
                }
            });
        }

        private void notifyProgress(final Object next) {
            if (job.getObserver() == null) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        job.getObserver().onNext(job, next);
                    }
                }
            });
        }

        @Override
        public void run() {
            if (job.isAborted()) {
                notifyCancellation();
                return ;
            }

            long loadTime = System.currentTimeMillis();
            Log.i("Scheduler", "Load Job[" + job.description() + "]");
            Iterator<Operator> operatorList = job.getOperatorList();
            Object tmp = job.getInput();
            while (operatorList.hasNext()) {
                Operator operator = operatorList.next();
                if (job.isAborted()) { // 当前任务是否取消
                    Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                    releaseResource(tmp);
                    notifyCancellation();
                    return ;
                }


                try {
                    if (UntilOperator.class.isInstance(operator)) {
                        UntilOperator untilOperator = (UntilOperator) operator; // 可能会执行多次
                        boolean notifyNextCallback = untilOperator.notifyNextCallback(); // 是否需压调用onNext回调
                        while (true) {
                            Object next = untilOperator.operate(tmp);
                            if (job.isAborted()) { // 当前任务是否取消
                                Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                                releaseResource(tmp);
                                notifyCancellation();
                                return ;
                            }
                            if (notifyNextCallback) { // 通知当前进度
                                notifyProgress(next);
                            }
                        }
                    } else {
                        tmp = operator.operate(tmp);
                    }
                } catch (Throwable throwable) {
                    throwable.printStackTrace();
                    Log.i("Scheduler", "Failed finish Job[" + job.description() + "]");
                    releaseResource(tmp);
                    long endTime = System.currentTimeMillis();
                    Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
                    return ;
                }
            }
            long endTime = System.currentTimeMillis();
            Log.i("Scheduler", "Success finish Job[" + job.description() + "]");
            Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
        }
    }

    private static void releaseResource(Object object) {
        if (null == object) {
            return;
        }

        if (Closeable.class.isInstance(object)) {
            Closeable closeable = (Closeable) object;
            IOUtil.closeQuietly(closeable);
        } else if (SixTuple.class.isInstance(object)) {
            SixTuple tuple = (SixTuple) object;
            releaseResource(tuple.value1);
            releaseResource(tuple.value2);
            releaseResource(tuple.value3);
            releaseResource(tuple.value4);
            releaseResource(tuple.value5);
            releaseResource(tuple.value6);
        } else if (FiveTuple.class.isInstance(object)) {
            FiveTuple tuple = (FiveTuple) object;
            releaseResource(tuple.value1);
            releaseResource(tuple.value2);
            releaseResource(tuple.value3);
            releaseResource(tuple.value4);
            releaseResource(tuple.value5);
        } else if (FourTuple.class.isInstance(object)) {
            FourTuple tuple = (FourTuple) object;
            releaseResource(tuple.value1);
            releaseResource(tuple.value2);
            releaseResource(tuple.value3);
            releaseResource(tuple.value4);
        } else if (ThreeTuple.class.isInstance(object)) {
            ThreeTuple tuple = (ThreeTuple) object;
            releaseResource(tuple.value1);
            releaseResource(tuple.value2);
            releaseResource(tuple.value3);
        } else if (TwoTuple.class.isInstance(object)) {
            TwoTuple tuple = (TwoTuple) object;
            releaseResource(tuple.value1);
            releaseResource(tuple.value2);
        } else if (Tuple.class.isInstance(object)) {
            Tuple tuple = (Tuple) object;
            releaseResource(tuple.value1);
        } else if (Tuples.class.isInstance(object)) {
            Tuples tuple = (Tuples) object;
            Object[] objects = tuple.values;
            if (null == objects || objects.length <= 0) {
                return;
            }
            for (Object obj : objects) {
                releaseResource(obj);
            }
        }
    }
}
