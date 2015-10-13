package org.dolphin.job.operator;

import org.dolphin.job.Operator;

/**
 * 封装成一个可循环使用的Operator.
 */
public class UntilOperator<I, R> implements Operator<I, R> {
    /** 原始的操作符 */
    private final Operator<I, R> rawOperator;
    /** 是否需要回调onext接口 {@link org.dolphin.job.Observer#onNext(Object)} */
    private final boolean needNotifyNext;
    public UntilOperator(Operator<I, R> operator){
        rawOperator = operator;
        needNotifyNext = false;
    }

    public UntilOperator(Operator<I, R> operator, final boolean notify){
        rawOperator = operator;
        needNotifyNext = notify;
    }

    @Override
    public R operator(I input) throws Throwable {
        return rawOperator.operator(input);
    }

    public boolean notifyNextCallback(){
        return needNotifyNext;
    }
}
