package org.dolphin.rv;

/**
 * Created by hanyanan on 2015/9/23.
 */
public abstract class RvSubscriber<T, R> {
    public abstract void onNext(T t);


    public abstract void onError(Throwable error);


    public abstract void onCompleted(R result);
}
