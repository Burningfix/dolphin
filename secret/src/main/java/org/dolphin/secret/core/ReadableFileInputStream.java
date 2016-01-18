package org.dolphin.secret.core;

import org.dolphin.lib.IOUtil;
import org.dolphin.secret.FileConstants;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class ReadableFileInputStream extends InputStream {
    private final File file;
    private FileInfo fileInfo = null;
    private long nextReadIndex = 0;
    private FileInfoContentCache contentCache;

    public ReadableFileInputStream(File file) {
        this.file = file;
    }

    @Override
    public int read() throws IOException {
        try {
            FileInfo fileInfo = getFileInfo();
            int transferSize = fileInfo.transferSize;
            if(nextReadIndex < transferSize) {
                // 需要从加密过的头部读取信息
                FileInfoContentCache cache = getContentCache();
                int index = (int) (nextReadIndex++);
                return cache.headBodyContent[index];
            }

            if(nextReadIndex >= fileInfo.originalFileLength - transferSize){
                // 需要从加密过的尾部读取信息
            }

        } finally {

        }
        return 0;
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return super.read(buffer);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        return super.read(buffer, byteOffset, byteCount);
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void mark(int readlimit) {
        super.mark(readlimit);
    }

    @Override
    public boolean markSupported() {
        return super.markSupported();
    }


    @Override
    public synchronized void reset() throws IOException {
        super.reset();
    }

    @Override
    public long skip(long byteCount) throws IOException {
        return super.skip(byteCount);
    }

    @Override
    public void close() throws IOException {
        super.close();
    }

    private FileInfoContentCache getContentCache() {
        if (null == contentCache) {
            contentCache
        }

        return contentCache;
    }

    private FileInfo getFileInfo() throws Throwable {
        if (null == fileInfo) {
            FileInfoReaderOperator operator = new FileInfoReaderOperator();
            fileInfo = operator.operate(file);
        }
        return fileInfo;
    }

    private static FileInfoContentCache createContentCache(File file, FileInfo fileInfo) throws FileNotFoundException, IOException {
        FileInfoContentCache cache = new FileInfoContentCache();
        FileInfo.Range headRange = fileInfo.originalFileHeaderRange;
        FileInfo.Range footRange = fileInfo.originalFileFooterRange;
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(footRange.offset);
            byte[] readedContent = new byte[footRange.count];
            randomAccessFile.readFully(readedContent);
            cache.footBodyContent = FileConstants.decode(readedContent);

            randomAccessFile.seek(headRange.offset);
            byte[] readedHeadContent = new byte[headRange.count];
            randomAccessFile.readFully(readedHeadContent);
            cache.headBodyContent = FileConstants.decode(readedHeadContent);
        } finally {
            IOUtil.safeClose(randomAccessFile);
        }

        return cache;
    }
}
