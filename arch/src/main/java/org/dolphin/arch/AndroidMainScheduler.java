package org.dolphin.arch;

import android.os.Handler;
import android.os.Looper;

import org.dolphin.job.schedulers.BaseScheduler;
import org.dolphin.job.schedulers.ExecutorScheduler;
import org.dolphin.job.schedulers.Scheduler;

import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledExecutorService;

/**
 * Created by hanyanan on 2016/1/13.
 */
public class AndroidMainScheduler {

    public static final Scheduler INSTANCE;

    static {
        final Handler handler = new Handler(Looper.getMainLooper());
        ExecutorScheduler executorScheduler = new ExecutorScheduler(new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        });

        INSTANCE = executorScheduler;
    }
}
