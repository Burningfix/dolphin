package org.dolphin.job.operator;

import org.dolphin.job.Operator;
import org.dolphin.lib.util.ValueUtil;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class StringToBytes implements Operator<String, byte[]> {
    @Override
    public byte[] operate(String input) throws Throwable {
        if(ValueUtil.isEmpty(input)) {
            return new byte[0];
        }
        return input.getBytes();
    }
}
