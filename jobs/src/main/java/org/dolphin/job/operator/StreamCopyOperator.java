package org.dolphin.job.operator;

import org.dolphin.job.tuple.ThreeTuple;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.util.IOUtil;
import java.io.InputStream;
import java.io.OutputStream;


public class StreamCopyOperator extends UntilOperator<ThreeTuple<InputStream,OutputStream,Long>,TwoTuple<Long,Long>> {
    private static final int BUFF_SIZE = 1024 * 64; // 64K
    private long cursor = 0;
    private boolean over = false;
    @Override
    public TwoTuple<Long, Long> operate(ThreeTuple<InputStream, OutputStream, Long> input) throws Throwable {
        long size = IOUtil.copy(input.value1, input.value2, BUFF_SIZE);
        if(size <= 0) {
            IOUtil.closeQuietly(input.value1);
            IOUtil.closeQuietly(input.value2);
            over = true;
            return null;
        }
        cursor += size;

        return new TwoTuple<Long, Long>(cursor, input.value3);
    }

    @Override
    public boolean over() {
        return this.over;
    }
}
