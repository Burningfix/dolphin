package org.dolphin.job.operator;

import org.dolphin.job.Operator;

/**
 * Created by hanyanan on 2015/11/17.
 */
public class SwallowExceptionOperator<T> implements Operator {
    private final Operator operator;
    private final T defaultValue;
    public SwallowExceptionOperator(Operator operator, T defaultValue) {
        this.operator = operator;
        this.defaultValue = defaultValue;
    }
    @Override
    public Object operate(Object input) throws Throwable {
        try{
            return operator.operate(input);
        }catch (Throwable throwable) {
            return this.defaultValue;
        }
    }
}
