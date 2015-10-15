package org.dolphin.job.schedulers;

import org.dolphin.job.*;
import org.dolphin.job.operator.UntilOperator;

import java.util.List;
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


    public synchronized static void loadJob(final Job job) {
        Subscription subscription = new Subscription();
        JobRunnable jobRunnable = new JobRunnable(job);

    }


    public synchronized static void loadJob(Job job, long delayTime, TimeUnit unit) {

    }

    public synchronized static void loadJob(Job job, long initialDelay, long period, TimeUnit unit) {

    }

    private static class JobRunnable implements Runnable {
        private final Job job;

        private JobRunnable(Job job) {
            this.job = job;
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
            for (Operator operator : operatorList) {
                if (job.isAborted()) { // 当前任务是否取消
                    Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                    notifyCancellation();
                    return;
                }

                try {
                    if (UntilOperator.class.isInstance(operator)) {
                        UntilOperator untilOperator = (UntilOperator) operator; // 可能会执行多次
                        boolean notifyNextCallback = untilOperator.notifyNextCallback(); // 是否需压调用onNext回调
                        while (true) {
                            Object next = untilOperator.operate(tmp);
                            if (next == null) { // 运行结束
                                break;
                            }
                            if (job.isAborted()) { // 当前任务是否取消
                                Log.i("Scheduler", "Cancel Job[" + job.description() + "]");
                                notifyCancellation();
                                return;
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
                    long endTime = System.currentTimeMillis();
                    Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
                    return;
                }
            }
            long endTime = System.currentTimeMillis();
            Log.i("Scheduler", "Success finish Job[" + job.description() + "]");
            Log.i("Scheduler", "Job[" + job.description() + "] Cost " + (endTime - loadTime) + "ms");
        }

        private Scheduler getObserverScheduler() {
            Scheduler scheduler = job.getObserverScheduler();
            if (null == scheduler) {
                scheduler = ImmediateScheduler.INSTANCE;
            }
            return scheduler;
        }

        private void notifyCancellation() {
            if(job.getObserver() == null) {
                return ;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    job.getObserver().onCancellation(job);
                }
            });
        }

        private void notifyComplete(final Object res) {
            if(job.getObserver() == null) {
                return ;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if(!job.isAborted()) {
                        job.getObserver().onCompleted(job, res);
                    }else{
                        job.getObserver().onCancellation(job);
                    }
                }
            });
        }

        private void notifyProgress(final Object next) {
            if(job.getObserver() == null) {
                return ;
            }
            getObserverScheduler().schedule(new Runnable() {
                @Override
                public void run() {
                    if(!job.isAborted()) {
                        job.getObserver().onNext(job, next);
                    }
                }
            });
        }
    }
}
