package org.dolphin.job.operator;

import org.dolphin.job.Operator;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class BytesToStringOperator implements Operator<byte[], String> {
    @Override
    public String operate(byte[] input) throws Throwable {
        return new String(input);
    }
}
