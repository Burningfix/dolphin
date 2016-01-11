package org.dolphin.job;

import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.*;
import org.dolphin.lib.IOUtil;

import java.io.Closeable;
import java.util.Iterator;
import java.util.List;
import java.util.WeakHashMap;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
class JobEngine {
    private final static String TAG = "JobEngine";
    private final static WeakHashMap<Object, Future> sJobReference = new WeakHashMap<Object, Future>();

    private JobEngine() throws IllegalAccessException {
        throw new IllegalAccessException("JobEngine cannot instance!");
    }

    public synchronized static void dispose() {
        // TODO
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

    public synchronized static void abort(Job job) {
        Future future = sJobReference.get(job);
        if (null != future && !future.isCancelled()) {
            future.cancel(true);
        }
        sJobReference.remove(job);
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
     * 调用取消的回调
     */
    private static void notifyCancellation(final Job job) {
        Job.Callback0 callback;
        synchronized (JobEngine.class) {
            callback = job.getCancelCallback();
        }
        if (null == callback) {
            return;
        }
        final Job.Callback0 call = callback;
        getCallbackScheduler(job).schedule(new Runnable() {
            @Override
            public void run() {
                call.call();
            }
        });
    }

    /**
     * 调用结果回调，
     * 1. 如果有回调，则调用结果回调，但是不释放资源，由回调函数中释放资源；
     * 2. 如果没有回调监听，则直接释放资源
     */
    private static void notifyResult(final Job job, final Object res) {
        Job.Callback1 callback;
        synchronized (JobEngine.class) {
            callback = job.getResultCallback();
        }
        if (null == callback) {
            releaseResource(res);
            return;
        }

        if (job.isAborted()) {
            releaseResource(res);
            notifyCancellation(job);
            return;
        }

        final Job.Callback1 call = callback;
        getCallbackScheduler(job).schedule(new Runnable() {
            @Override
            public void run() {
                if (!job.isAborted()) {
                    call.call(res);
                } else {
                    releaseResource(res);
                    Job.Callback0 callback = null;
                    synchronized (JobEngine.class) {
                        callback = job.getCancelCallback();
                    }
                    if (null != callback) {
                        callback.call();
                    }
                }
            }
        });
    }

    /**
     * 调用错误的回调
     */
    private static void notifyError(final Job job, final Throwable error, final Object... unExpectResult) {
        Job.Callback2 errorCallback;
        synchronized (JobEngine.class) {
            errorCallback = job.getErrorCallback();
        }

        if (null == errorCallback) {
            releaseResource(unExpectResult);
            return;
        }

        if (job.isAborted()) {
            releaseResource(unExpectResult);
            notifyCancellation(job);
            return;
        }

        final Job.Callback2 call = errorCallback;
        getCallbackScheduler(job).schedule(new Runnable() {
            @Override
            public void run() {
                if (!job.isAborted()) {
                    call.call(error, unExpectResult);
                } else {
                    releaseResource(unExpectResult);
                    Job.Callback0 callback;
                    synchronized (JobEngine.class) {
                        callback = job.getCancelCallback();
                    }
                    if (null != callback) {
                        callback.call();
                    }
                }
            }
        });
    }

    /**
     * 最终运行都是JobRunnable。
     */
    private static class JobRunnable implements Runnable, Comparable<JobRunnable> {
        private final Job job;

        private JobRunnable(Job job) {
            this.job = job;
        }

        @Override
        public void run() {
            if (job.isAborted()) {
                notifyCancellation(job);
                return;
            }
            long loadTime = System.currentTimeMillis();
            Log.i(TAG, "Load Job[" + job.toString() + "]");
            List<Operator> operatorList = job.getOperators();
            Object tmp = job.getInput();
            while (operatorList.size() > 0) {
                Operator operator = operatorList.remove(0);
                if (job.isAborted()) { // 当前任务是否取消
                    Log.i(TAG, "Cancel Job cost " + (System.currentTimeMillis() - loadTime) + " [" + job.toString() + "]");
                    releaseResource(tmp);
                    notifyCancellation(job);
                    return;
                }

                try {
                    if (UntilOperator.class.isInstance(operator)) {
                        UntilOperator untilOperator = (UntilOperator) operator; // 可能会执行多次
                        while (true) {
                            Object progressValue = untilOperator.operate(tmp);
                            if (job.isAborted()) { // 当前任务是否取消
                                Log.i(TAG, "Cancel Job cost " + (System.currentTimeMillis() - loadTime) + " [" + job.toString() + "]");
                                notifyCancellation(job);
                                releaseResource(progressValue);
                                releaseResource(tmp);
                                return;
                            }
                            //TODO:  notify progress
                            if (untilOperator.over()) { // 运行结束
                                releaseResource(progressValue);
                                releaseResource(tmp);
                                break;
                            }
                        }
                    } else {
                        tmp = operator.operate(tmp);
                    }
                } catch (Throwable throwable) {
                    Log.d(TAG, throwable.getMessage());
                    throwable.printStackTrace();
                    long endTime = System.currentTimeMillis();
                    Log.i(TAG, "Failed Job cost " + (endTime - loadTime) + " [" + job.toString() + "]");
                    notifyError(job, throwable);
                    releaseResource(tmp);
                    abort(job);
                    return;
                }
            }
            long endTime = System.currentTimeMillis();
            Log.i(TAG, "Complete Job cost " + (endTime - loadTime) + " [" + job.toString() + "]");
            notifyResult(job, tmp);
        }

        @Override
        public int compareTo(JobRunnable o) {
            return job.compareTo(o.job);
        }
    }

    public static void releaseResource(Object object) {
        if (null == object) {
            return;
        }

        if (Iterable.class.isInstance(object)) {
            Iterable iterable = (Iterable) object;
            Iterator iterator = iterable.iterator();
            while (iterator.hasNext()) {
                releaseResource(iterator.next());
            }
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
