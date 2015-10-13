package org.dolphin.job;

/**
 * Created by hanyanan on 2015/10/9.
 */
public interface Observer<I, R> {
    public void onNext(I next);

    public void onCompleted(R result);

    public void onFailed();
}
