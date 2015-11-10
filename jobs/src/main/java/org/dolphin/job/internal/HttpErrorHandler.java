package org.dolphin.job.internal;

import org.dolphin.job.Job;
import org.dolphin.job.JobErrorHandler;
import org.dolphin.job.JobRunningResult;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class HttpErrorHandler implements JobErrorHandler {
    public static int sMaxCount = 3;
    private int maxCount;
    private int currCount = 1;

    public HttpErrorHandler(int maxCount) {
        this.maxCount = maxCount;
    }

    public HttpErrorHandler() {
        this.maxCount = sMaxCount;
    }

    @Override
    public JobRunningResult handleError(Job job, Throwable throwable) {
        if(currCount > maxCount) return JobRunningResult.TERMINATE;
        currCount++;
        return JobRunningResult.CONTINUE;
    }
}
