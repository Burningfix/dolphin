package org.dolphin.job;

import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.*;
import org.dolphin.job.util.Log;
import org.dolphin.lib.IOUtil;

import java.io.Closeable;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class JobEngine {
    private final static WeakHashMap<Object, Future> sJobReference = new WeakHashMap<Object, Future>();

    public synchronized static void dispose() {

    }

    public synchronized static void loadJob(final Job job) {
        JobWorkPolicy workPolicy = job.getWorkPolicy();
        workPolicy = workPolicy == null ? JobWorkPolicy.DEFAULT_JOB_WORK_POLICY : workPolicy;
        if (null == workPolicy) {
            JobRunnable jobRunnable = new JobRunnable(job);
            Future future = getWorkScheduler(job).schedule(jobRunnable);
            sJobReference.put(job, future);
        } else {
            if (workPolicy.periodicNanoTime() > 0) {
                // 周期性的
                loadJob(job, workPolicy.delayNanoTime(), workPolicy.periodicNanoTime(), TimeUnit.NANOSECONDS);
            } else {
                // 非周期性的
                loadJob(job, workPolicy.delayNanoTime(), TimeUnit.NANOSECONDS);
            }
        }
    }

    private synchronized static void loadJob(Job job, long delayTime, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Future future = getWorkScheduler(job).schedule(jobRunnable, delayTime, unit);
        sJobReference.put(job, future);
    }

    private synchronized static void loadJob(Job job, long initialDelay, long period, TimeUnit unit) {
        JobRunnable jobRunnable = new JobRunnable(job);
        Future future = getWorkScheduler(job).schedulePeriodically(jobRunnable, initialDelay, period, unit);
        sJobReference.put(job, future);
    }

    public synchronized void abort(Job job) {
        Future future = sJobReference.get(job);
        if (null != future && !future.isCancelled()) {
            future.cancel(true);
        }
    }

    /**
     * Get default work schedule for specify job.
     */
    public static Scheduler getWorkScheduler(final Job job) {
        Scheduler scheduler = job.getWorkScheduler();
        return scheduler == null ? Schedulers.computation() : scheduler;
    }

    /**
     * 得到回调的工作的线程
     */
    public static Scheduler getCallbackScheduler(final Job job) {
        Scheduler scheduler = job.getCallbackScheduler();
        return scheduler == null ? Schedulers.immediate() : scheduler;
    }


    /**
     * 最终运行都是JobRunnable。
     */
    private static class JobRunnable implements Runnable, Comparable<JobRunnable> {
        private final Job job;

        private JobRunnable(Job job) {
            this.job = job;
        }

        private void notifyCancellation() {
            final Job.Callback0 callback = job.getCancelCallback();
            if (null == callback) {
                return;
            }
            getCallbackScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    callback.call();
                }
            });
        }

        private void notifyResult(final Object res) {
            final Job.Callback1 callback = job.getResultCallback();
            if (null == callback) {
                releaseResource(res);
            } else {
                getCallbackScheduler().schedule(new Runnable() {
                    @Override
                    public void run() {
                        if (!job.isAborted()) {
                            callback.call(res);
                        } else {
                            Job.Callback0 callback = job.getCancelCallback();
                            if (null != callback) {
                                callback.call();
                            }
                        }
                        releaseResource(res);
                    }
                });
            }
        }

        private void notifyError(final Throwable error, final Object ... unExpectResult) {
            final Job.Callback2 errorCallback = job.getErrorCallback();
            if (null == errorCallback) {
                return;
            }
            getCallbackScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if (!job.isAborted()) {
                        errorCallback.call(error, unExpectResult);
                    } else {
                        Job.Callback0 callback = job.getCancelCallback();
                        if (null != callback) {
                            callback.call();
                        }
                    }
                    releaseResource(unExpectResult);
                }
            });
        }

//        private void notifyProgress(final Object next) {
//            if (getCallbackScheduler() == null) {
//                return;
//            }
//            final Observer observer = job.getObserver();
//            if (null == observer) {
//                return;
//            }
//            getCallbackScheduler().schedule(new Runnable() {
//                @Override
//                public void run() {
//                    if (!job.isAborted()) {
//                        observer.onNext(job, next);
//                    }
//                }
//            });
//        }

        @Deprecated
        private void notifyPreJobRunning(final Job job) {
            final JobRunningState runningState = job.getRunningState();
            if (null == runningState) return;
            Scheduler scheduler = getCallbackScheduler();
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    runningState.preRunningJob(job);
                }
            });
        }

        @Deprecated
        private void notifyPostJobRunning(final Job job) {
            final JobRunningState runningState = job.getRunningState();
            if (null == runningState) return;
            Scheduler scheduler = getCallbackScheduler();
            scheduler.schedule(new Runnable() {
                @Override
                public void run() {
                    runningState.postRunningJob(job);
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
            List<Operator> operatorList = job.getOperators();
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
                    Log.d("Scheduler", throwable.getMessage());
                    throwable.printStackTrace();
                    Log.i("Scheduler", "Failed running Job[" + job.description() + "]");
                    long endTime = System.currentTimeMillis();
                    Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
                    JobErrorHandler handler = getErrorHandler(job);
                    JobRunningResult runningResult = handler.handleError(job, throwable);
                    if (runningResult == JobRunningResult.TERMINATE) {
                        notifyFailed(job, throwable);
                        releaseResource(tmp);
                        abort(job);
                    } else {
                        if (tmp != job.getInput()) {
                            releaseResource(tmp);
                        }
                        abort(job);
                        Log.i("Scheduler", "Prepare running Job again[" + job.description() + "]");
                        JobWorkPolicy workPolicy = job.getWorkPolicy();
                        loadJob(job.setWorkPolicy(JobWorkPolicy.workPolicy(workPolicy.periodicNanoTime(), workPolicy.periodicNanoTime(), TimeUnit.NANOSECONDS)));
                    }
                    return;
                }
            }
            long endTime = System.currentTimeMillis();
            notifyComplete(tmp);
            Log.i("Scheduler", "Success finish Job[" + job.description() + "]");
            Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
        }

        private JobErrorHandler getErrorHandler(final Job job) {
            JobErrorHandler handler = job.getErrorHandler();
            if (handler == null) return JobErrorHandler.TERMINATE_HANDLER;
            return handler;
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
