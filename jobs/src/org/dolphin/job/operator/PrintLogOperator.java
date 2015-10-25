package org.dolphin.job.operator;

import org.dolphin.job.Log;
import org.dolphin.job.Operator;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class PrintLogOperator implements Operator<String, String> {

    @Override
    public String operate(String input) throws Throwable {
        Log.d("", input);
        return input;
    }
}
