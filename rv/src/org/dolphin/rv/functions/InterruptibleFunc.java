package org.dolphin.rv.functions;

import org.dolphin.rv.RvSubscriber;

/**
 * Created by hanyanan on 2015/9/24.
 *
 *
 *
 *
 *
 * 可被中断
 */
public interface InterruptibleFunc<T, R> {
    public R call(T input, RvSubscriber subscriber) throws Throwable;
}
