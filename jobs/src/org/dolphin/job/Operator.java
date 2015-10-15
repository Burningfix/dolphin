package org.dolphin.job;

/**
 * Created by hanyanan on 2015/9/25.
 *
 *
 * {@link InterruptibleOperator} 非原子操作的运算符
 * 原子操作的运算符
 */
public interface Operator<I, R> {

    /**
     * 接受一个输入，进行计算，产生一个输出
     * @param input 接受的输入参数
     * @return 计算后的输出
     */
    public R operate(I input) throws Throwable;
}
