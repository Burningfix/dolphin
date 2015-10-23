package org.dolphin.job.schedulers;

import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class IOScheduler extends BaseScheduler {
    private PriorityScheduledThreadPoolExecutor scheduledExecutorService;

    public IOScheduler() {
        scheduledExecutorService = new PriorityScheduledThreadPoolExecutor(1, 1, 1, TimeUnit.SECONDS,
                new PriorityBlockingQueue<Runnable>(), sRejectedExecutionHandler);
    }

    @Override
    public ScheduledExecutorService getWorkExecutor() {
        return scheduledExecutorService;
    }

    private static RejectedExecutionHandler sRejectedExecutionHandler = new RejectedExecutionHandler() {
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            // TODO
        }
    };

    @Override
    public void pause() {
        scheduledExecutorService.pause();
    }

    @Override
    public void resume() {
        scheduledExecutorService.resume();
    }
}
