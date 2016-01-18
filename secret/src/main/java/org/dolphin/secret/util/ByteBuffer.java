package org.dolphin.secret.util;

import java.io.IOException;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2016/1/16.
 */
public class ByteBuffer {
    public static final int BLOCK_SIZE = 1024;
    private final List<byte[]> data = new LinkedList<byte[]>();
    private int nextWriteOffset = 0;
    private byte[] currWriteBlock = null;
    private int nextReadOffset = 0;
    private byte[] currReadBlock = null;
    private int size = 0;

    public byte[] generateBytes(int size) {
        return new byte[size];
    }

    public void releaseBytes(byte[] bytes) {
        // Do nothing
    }

    public void write(int oneByte) throws IOException {
        if (null == currWriteBlock || nextWriteOffset >= BLOCK_SIZE) {
            currWriteBlock = generateBytes(BLOCK_SIZE);
            nextWriteOffset = 0;
            data.add(currWriteBlock);
        }

        currWriteBlock[nextWriteOffset++] = (byte) oneByte;
        ++size;
    }


    public void write(byte[] buffer, int offset, int count) throws IOException {
        checkStartAndEnd(buffer.length, offset, count);
        do {
            if (null == currWriteBlock || nextWriteOffset >= BLOCK_SIZE) {
                currWriteBlock = generateBytes(BLOCK_SIZE);
                nextWriteOffset = 0;
                data.add(currWriteBlock);
            }
            int currBlockLeave = BLOCK_SIZE - nextWriteOffset;
            int copyLength = currBlockLeave >= count ? count : currBlockLeave;
            System.arraycopy(buffer, offset, currWriteBlock, nextWriteOffset, copyLength);
            offset += copyLength;
            nextWriteOffset += copyLength;
        } while (count > 0);
    }


    public byte read() {
//        if (null == currReadBlock) {
//            nextReadOffset
//
//        }


            return 0;
    }

    public byte[] read(int count) {
        return null;
    }

    public byte get(int index) {
        return 0;
    }

    public byte[] get(int offset, int count) {
        return null;
    }

    public int getSize() {
        return size;
    }


    /**
     * Checks that the range described by {@code start} and {@code end} doesn't exceed
     * {@code len}.
     */
    public static void checkStartAndEnd(int len, int start, int end) {
        if (start < 0 || end > len) {
            throw new ArrayIndexOutOfBoundsException("start < 0 || end > len."
                    + " start=" + start + ", end=" + end + ", len=" + len);
        }
        if (start > end) {
            throw new IllegalArgumentException("start > end: " + start + " > " + end);
        }
    }
}
