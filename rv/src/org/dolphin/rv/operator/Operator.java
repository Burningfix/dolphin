package org.dolphin.rv.operator;

/**
 * Created by hanyanan on 2015/9/25.
 *
 *
 * {@link NonAtomicOperator} 非原子操作的运算符
 * 原子操作的运算符
 */
public interface Operator<I, R> {

    /**
     * 接受一个输入，进行计算，产生一个输出
     * @param input
     * @return
     */
    public R operator(I input) throws Throwable;
}
