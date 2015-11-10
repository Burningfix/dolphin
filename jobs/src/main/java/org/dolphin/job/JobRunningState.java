package org.dolphin.job;

/**
 * Created by hanyanan on 2015/11/10.
 */
public interface JobRunningState {
    public JobRunningResult preRunningJob(Job job);

    public JobRunningResult postRunningJob(Job job);
}
