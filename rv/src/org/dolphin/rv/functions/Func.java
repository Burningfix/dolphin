package org.dolphin.rv.functions;

/**
 * Created by hanyanan on 2015/9/17.
 * <p/>
 * <p/>
 * Receive input object and output another object.
 * <p/>
 * No matter when/what/how to call this function, it will get the
 * <p/>
 * 接受一个输入参数，产生一个输出， 是所有最小的执行单元。
 * 一个任务可以分解为多个任务的
 *
 * <b>此方法不可被中断</b>
 */
public interface Func<T, R> {
    public R call(T input) throws Throwable;
}
