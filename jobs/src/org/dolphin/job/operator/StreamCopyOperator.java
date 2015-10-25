package org.dolphin.job.operator;

import org.dolphin.job.Operator;
import org.dolphin.job.tuple.TwoTuple;

import java.io.InputStream;
import java.io.OutputStream;

/**
 * Created by yananh on 2015/10/25.
 */
public class StreamCopyOperator implements Operator<TwoTuple<InputStream, OutputStream>, TwoTuple<Long, Long>> {
    private long cursor = 0;
    @Override
    public TwoTuple<Long, Long> operate(TwoTuple<InputStream, OutputStream> input) throws Throwable {
        return null;
    }
}
