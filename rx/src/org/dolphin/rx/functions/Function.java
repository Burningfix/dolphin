package org.dolphin.rx.functions;

/**
 * Created by hanyanan on 2015/9/17.
 */
public interface Function<T, R> {
    public R call(T t);
}
