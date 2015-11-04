package org.dolphin.job;

/**
 * Created by hanyanan on 2015/10/13.
 */
public interface JobErrorHandler {
    public Job handleError(Job job, Throwable throwable) throws Throwable;
}
