package org.dolphin.hotpatch.dex;

import android.os.Handler;
import android.os.Looper;

import org.dolphin.job.schedulers.ExecutorScheduler;
import org.dolphin.job.schedulers.Scheduler;

import java.util.concurrent.Executor;

/**
 * Created by yananh on 2015/11/7.
 */
public class AndroidMainThreadScheduler extends ExecutorScheduler implements Scheduler {
    public static final AndroidMainThreadScheduler INSTANCE = new AndroidMainThreadScheduler(new Handler(Looper.getMainLooper()));
    private final Handler handler;
    public AndroidMainThreadScheduler(final Handler handler) {
        super(new Executor() {
            @Override
            public void execute(Runnable command) {
                handler.post(command);
            }
        });

        this.handler = handler;
    }

    @Override
    public void pause() {
        throw new UnsupportedOperationException("AndroidMainThreadScheduler Not support pause function!");
    }

    @Override
    public void resume() {
        throw new UnsupportedOperationException("AndroidMainThreadScheduler Not support resume function!");
    }
}
