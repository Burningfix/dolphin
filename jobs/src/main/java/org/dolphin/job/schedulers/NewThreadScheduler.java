package org.dolphin.job.schedulers;

import java.util.concurrent.LinkedBlockingDeque;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/28.
 */
public class NewThreadScheduler extends BaseScheduler {
    private PriorityScheduledThreadPoolExecutor scheduledExecutorService;
    @Override
    public ScheduledExecutorService getWorkExecutor() {
        if(null == scheduledExecutorService) {
            scheduledExecutorService = new PriorityScheduledThreadPoolExecutor(1, 1,
            1, TimeUnit.MINUTES, new LinkedBlockingQueue<Runnable>());
        }
        return scheduledExecutorService;
    }

    @Override
    public void pause() {
        scheduledExecutorService.pause();
    }

    @Override
    public void resume() {
        scheduledExecutorService.resume();
    }
}
