package org.dolphin.job.schedulers;

import org.dolphin.job.Job;
import org.dolphin.job.JobErrorHandler;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.tuple.*;
import org.dolphin.job.util.Log;
import org.dolphin.lib.IOUtil;

import java.io.Closeable;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class JobEngine {


    private static JobEngine instance = null;

    public synchronized static JobEngine instance() {
        if (null == instance) {
            instance = new JobEngine();
        }

        return instance;
    }

    private JobEngine() {

    }

    private final WeakHashMap<Object, Subscription> jobReference = new WeakHashMap<Object, Subscription>();

    public synchronized void loadJob(final Job job) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedule(jobRunnable);
        jobReference.put(job, subscription);
    }

    public synchronized void loadJob(Job job, long delayTime, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedule(jobRunnable, delayTime, unit);
        jobReference.put(job, subscription);
    }

    public synchronized void loadJob(Job job, long initialDelay, long period, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Subscription subscription = getWorkScheduler(job).schedulePeriodically(jobRunnable, initialDelay, period, unit);
        jobReference.put(job, subscription);
    }

    public synchronized void abort(Job job) {
        Subscription subscription = jobReference.get(job);
        if (!subscription.isUnsubscription()) {
            subscription.unsubscription();
        }
    }

    /**
     * Get default work schedule for specify job.
     */
    private static Scheduler getWorkScheduler(final Job job) {
        Scheduler scheduler = job.getWorkScheduler();
        if (null == scheduler) {
            return Schedulers.computation();
        }
        return scheduler;
    }


    /**
     * 最终运行都是JobRunnable。
     */
    private static class JobRunnable implements Runnable, Comparable<JobRunnable> {
        private final Job job;

        private JobRunnable(Job job) {
            this.job = job;
        }

        private Scheduler getObserverScheduler() {
            Scheduler scheduler = job.getObserverScheduler();
            if (null == scheduler) {
                scheduler = Schedulers.immediate();
            }
            return scheduler;
        }

        private void notifyCancellation() {
            if (getObserverScheduler() == null) {
                return;
            }
            final Observer observer = job.getObserver();
            if (null == observer) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    observer.onCancellation(job);
                }
            });
        }

        private void notifyComplete(final Object res) {
            if (getObserverScheduler() == null) {
                return;
            }
            final Observer observer = job.getObserver();
            if (null == observer) {
                releaseResource(res);
            } else {
                getObserverScheduler().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!job.isAborted()) {
                            observer.onCompleted(job, res);
                        } else {
                            observer.onCancellation(job);
                        }
                        releaseResource(res);
                    }
                });
            }
        }

        private void notifyFailed(final Job job, final Throwable error) {
            if (getObserverScheduler() == null) {
                return;
            }
            final Observer observer = job.getObserver();
            if (null == observer) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        observer.onFailed(job, error);
                    } else {
                        observer.onCancellation(job);
                    }
                }
            });
        }

        private void notifyProgress(final Object next) {
            if (getObserverScheduler() == null) {
                return;
            }
            final Observer observer = job.getObserver();
            if (null == observer) {
                return;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        observer.onNext(job, next);
                    }
                }
            });
        }

        @Override
        public void run() {
            if (job.isAborted()) {
                notifyCancellation();
                return;
            }

            long loadTime = System.currentTimeMillis();
            Log.i("Scheduler", "Load Job[" + job.description() + "]");
            List<Operator> operatorList = job.getOperatorList();
            Object tmp = job.getInput();
            while (operatorList.size() > 0) {
                Operator operator = operatorList.remove(0);
                if (job.isAborted()) { // 当前任务是否取消
                    Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                    releaseResource(tmp);
                    notifyCancellation();
                    return;
                }

                try {
                    if (UntilOperator.class.isInstance(operator)) {
                        UntilOperator untilOperator = (UntilOperator) operator; // 可能会执行多次
                        boolean notifyNextCallback = untilOperator.notifyNextCallback(); // 是否需压调用onNext回调
                        while (true) {
                            Object next = untilOperator.operate(tmp);
                            if (job.isAborted()) { // 当前任务是否取消
                                Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                                notifyCancellation();
                                releaseResource(next);
                                releaseResource(tmp);
                                return;
                            }
                            if (null == next) {
                                break;
                            }
                            if (notifyNextCallback) { // 通知当前进度
                                notifyProgress(next);
                            }
                            releaseResource(next);
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
                    JobErrorHandler handler = job.getErrorHandler();
                    try {
                        if(null != handler){
                            handler.handleError(job, throwable);
                        }
                    } catch (Throwable throwable1) {
                        throwable1.printStackTrace();
                        notifyFailed(job, throwable);
                    }

                    return;
                }
            }
            long endTime = System.currentTimeMillis();
            notifyComplete(tmp);
            Log.i("Scheduler", "Success finish Job[" + job.description() + "]");
            Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
        }

        @Override
        public int compareTo(JobRunnable o) {
            return job.compareTo(o.job);
        }
    }

    private static void releaseResource(Object object) {
        if (null == object) {
            return;
        }

        if (Closeable.class.isInstance(object)) {
            Closeable closeable = (Closeable) object;
            IOUtil.closeQuietly(closeable);
        } else if (!Tupleable.class.isInstance(object)) {
            // 普通的数据，不需要释放
            return;
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
