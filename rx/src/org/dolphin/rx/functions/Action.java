package org.dolphin.rx.functions;

/**
 * Created by hanyanan on 2015/9/17.
 *
 * The callback used to
 */
public interface Action<T> {
    public void call(T t);
}
