package org.dolphin.job.operator;

import org.dolphin.job.Operator;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class PrintTimeOperator implements Operator {
    @Override
    public Object operate(Object input) throws Throwable {
        System.out.println(System.currentTimeMillis());
        return null;
    }
}
