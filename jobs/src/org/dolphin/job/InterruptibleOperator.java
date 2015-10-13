package org.dolphin.job;

/**
 * Created by hanyanan on 2015/9/25.
 *
 *
 * {@link Operator}
 *
 * 可中断的操作符
 */
public interface InterruptibleOperator<I, R> {

    public R operator(I input) throws Throwable;
}
