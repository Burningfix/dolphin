package org.dolphin.job;

/**
 * Created by hanyanan on 2015/10/9.
 */
public interface Observer<I, R> {
    public void onNext(Job job, I next);

    public void onCompleted(Job job, R result);

    public void onFailed(Job job);

    public void onCancellation(Job job);
}
