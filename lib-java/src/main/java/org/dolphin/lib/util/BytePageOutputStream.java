package org.dolphin.lib.util;


import java.io.IOException;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Random;
import java.util.Set;

/**
 * Created by yananh on 2016/1/16.
 */
public class BytePageOutputStream extends OutputStream {
    private final String TAG = "BytePageOutputStream";
    public static final int PAGE_SIZE = 1024;
    public static final int MAX_PAGE_COUNT = 512;
    private static int sMaxPageCount = MAX_PAGE_COUNT;
    private static final List<byte[]> sFreePagePool = new LinkedList<byte[]>();
    private static final Set sPagePool = new HashSet();

    public synchronized static void maxSize(int maxSize) {

    }

    public synchronized static void maxPage(int count) {

    }


    private synchronized static byte[] alloc() {
        if (sFreePagePool.size() > 0) {
            return sFreePagePool.remove(0);
        }

        if (sPagePool.size() < sMaxPageCount) {
            int maxAllocPageCount = sMaxPageCount - sPagePool.size();
            List<byte[]> fresh = allocPages(maxAllocPageCount > 20 ? 20 : maxAllocPageCount);
            sFreePagePool.addAll(fresh);
            sPagePool.addAll(fresh);
        }

        if (sFreePagePool.size() > 0) {
            return sFreePagePool.remove(0);
        }
        return null;
    }

    private synchronized static void free(byte[] block) {
        if (!sPagePool.contains(block)) {
            return;
        }
        sFreePagePool.add(0, block);
    }

    private synchronized static void free(List<byte[]> pages) {
        if (null == pages || pages.isEmpty()) {
            return;
        }
        if (sPagePool.containsAll(pages)) {
            sFreePagePool.addAll(pages);
            return;
        }
        for (byte[] page : pages) {
            if (sPagePool.contains(page)) {
                sFreePagePool.add(0, page);
            }
        }
    }

    public synchronized static void trim() {
        final int maxPage = sMaxPageCount;
        int allocatedPageSize = sPagePool.size();
        if (allocatedPageSize <= maxPage) {
            return;
        }
        // 已经分配的page数量>期望page的数量
        // gap为需要释放的数量
        int gap = allocatedPageSize - maxPage;
        if (gap <= sFreePagePool.size()) {
            // 可以释放当前可用page一部分的page
            List<byte[]> deletes = sFreePagePool.subList(0, gap - 1);
            sFreePagePool.removeAll(deletes);
            sPagePool.removeAll(deletes);
        } else {
            // 释放全部的当前可用page还是不够，需要释放的更多
            int usefulPageCount = sFreePagePool.size();
            sPagePool.removeAll(sFreePagePool);
            sFreePagePool.clear();
            for (int i = 0; sPagePool.size() > 0 && i < gap - usefulPageCount; ++i) {
                sPagePool.remove(sFreePagePool.get(0));
            }
        }
    }

    private static List<byte[]> allocPages(int count) {
        List<byte[]> res = new ArrayList<byte[]>();
        for (int i = 0; i < count; ++i) {
            res.add(new byte[PAGE_SIZE]);
        }
        return res;
    }


    private final List<byte[]> pagePool = new LinkedList<byte[]>();
    private int offset = 0;

    @Override
    public void close() throws IOException {
        free(pagePool);
        pagePool.clear();
        super.close();
    }

    @Override
    public void flush() throws IOException {
        super.flush();
    }

    @Override
    public void write(byte[] buffer) throws IOException {
        super.write(buffer);
    }

    // 申请一个新的页面到当前页面
    private void applyPage() {
        byte[] page = alloc();
        if (null == page) {
            page = new byte[PAGE_SIZE];
        }
        this.pagePool.add(page);
    }

    @Override
    public void write(byte[] buffer, final int offset, final int count) throws IOException {
        // TODO check offset and size
        synchronized (this) {
            int pageIndex = 0, pageOffset = 0;
            pageIndex = this.offset / PAGE_SIZE;
            pageOffset = this.offset % PAGE_SIZE;
            while (pageIndex >= pagePool.size()) {
                applyPage();
            }
            byte[] currPage = pagePool.get(pageIndex);
            int currentPageRetain = PAGE_SIZE - pageOffset; // 当前page剩余的槽位
            if (currentPageRetain >= count) {
                // 全部写入到当前页面
                System.arraycopy(buffer, offset, currPage, pageOffset, count);
                this.offset += count;
            } else {
                // 当前页面只能写入一部分
                System.arraycopy(buffer, offset, currPage, pageOffset, currentPageRetain);
                this.offset += currentPageRetain;
                write(buffer, offset + currentPageRetain, count - currentPageRetain);
            }
        }
    }

    @Override
    public void write(int oneByte) throws IOException {
        write(new byte[]{(byte) oneByte});
    }

    public int size() {
        return offset;
    }

    public byte[] toByteArray() {
        synchronized (this) {
            byte[] newArray = new byte[offset];
            int size = this.offset;
            int index = 0;
            while (size > 0) {
                int max = size > PAGE_SIZE ? PAGE_SIZE : size;
                System.arraycopy(pagePool.get(index++), 0, newArray, newArray.length - size, max);
                size -= max;
            }

            return newArray;
        }
    }

    public static void main(String[] argv) {
        byte[] source = new byte[1024 * 1234 + 345];
        Random random = new Random();
        random.nextBytes(source);

        BytePageOutputStream outputStream = new BytePageOutputStream();
        try {
            outputStream.write(source, 0, source.length);
            byte[] target = outputStream.toByteArray();
            System.out.println("Source Length " + source.length + " , Target length " + target.length);
            for (int i = 0; i < source.length; ++i) {
                if (target[i] != source[i]) {
                    System.out.println("Unmatch at index " + i);
                    return;
                }
            }
            System.out.println("All Match!");
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
