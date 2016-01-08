package org.dolphin.job.operator;

import org.dolphin.job.Operator;

/**
 * 封装成一个可循环使用的Operator.
 */
public abstract class UntilOperator<I, R> implements Operator<I, R> {
    public abstract boolean over();
}
