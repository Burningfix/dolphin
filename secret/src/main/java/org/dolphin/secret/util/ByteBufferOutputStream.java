package org.dolphin.secret.util;

import org.dolphin.job.Log;

import java.io.IOException;
import java.io.OutputStream;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

/**
 * Created by yananh on 2016/1/16.
 */
public class ByteBufferOutputStream extends OutputStream {
    private final String TAG = "ByteBufferOutputStream";
    public static final int BLOCK_SIZE = 1024;
    public static final int MAX_BLOCK_COUNT = 512;
    private static final List<byte[]> sBuffPool = new LinkedList<byte[]>();
    private static final Set sBuffPoolRefs = new HashSet();

    public synchronized static void maxSize(int maxSize) {

    }

    public synchronized static void maxBlockCount(int count) {

    }

    public synchronized static void trim() {

    }

    private synchronized static byte[] alloc() {
        if (sBuffPool.size() <= 0) {
            return null;
        }
        return sBuffPool.remove(0);
    }

    private synchronized static void free(byte[] block) {
        if (!sBuffPoolRefs.contains(block)) {
            Log.e(TAG, "Free a invalidate block!!!!!");
            return;
        }
        sBuffPool.add(0, block);
    }


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

    public int getSize() {
        return size;
    }
}
