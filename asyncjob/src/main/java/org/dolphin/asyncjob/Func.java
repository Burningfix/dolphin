package org.dolphin.asyncjob;

/**
 * Created by yananh on 2016/1/1.
 */
public interface Func<I, O> {
    public O func(I in) throws Throwable;
}
