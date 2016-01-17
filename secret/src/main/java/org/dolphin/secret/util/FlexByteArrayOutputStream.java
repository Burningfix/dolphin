package org.dolphin.secret.util;

import java.io.IOException;
import java.io.OutputStream;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2016/1/16.
 */
public class FlexByteArrayOutputStream extends OutputStream {
    public static final int BLOCK_SIZE = 1024;
    private final List<byte[]> data = new LinkedList<byte[]>();
    private int nextOffset = 0;
    private int size = 0;
    private byte[] currBlock = null;

    @Override
    public void write(int oneByte) throws IOException {
        if (null == currBlock || nextOffset >= BLOCK_SIZE) {
            currBlock = new byte[BLOCK_SIZE];
            nextOffset = 0;
            data.add(currBlock);
        }

        currBlock[nextOffset++] = (byte) oneByte;
        ++size;
    }

    @Override
    public void close() throws IOException {
        data.clear();
        super.close();
    }

    public int getSize(){
        return size;
    }
}
