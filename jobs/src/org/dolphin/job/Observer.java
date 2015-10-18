package org.dolphin.job;

import org.dolphin.job.tuple.ThreeTuple;

/**
 * Created by hanyanan on 2015/10/9.
 */
public interface Observer<I, R> {
    public void onNext(Job job, I next);

    public void onCompleted(Job job, R result);

    public void onFailed(Job job, Throwable error);

    public void onCancellation(Job job);
}
