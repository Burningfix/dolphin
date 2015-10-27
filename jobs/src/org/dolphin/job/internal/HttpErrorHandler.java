package org.dolphin.job.internal;

import org.dolphin.job.Job;
import org.dolphin.job.JobErrorHandler;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class HttpErrorHandler implements JobErrorHandler {
    public static int sMaxCount = 3;
    private int count;
    private int currCount = 1;

    public HttpErrorHandler(int count) {
        this.count = count;
    }

    public HttpErrorHandler() {
        this.count = sMaxCount;
    }

    @Override
    public Job handleError(Job job, Throwable throwable) throws Throwable {
        if(currCount > count) throw throwable;
        currCount++;
        return job;
    }
}
