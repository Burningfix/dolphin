package org.dolphin.secret.core;

import android.util.Log;

import org.dolphin.lib.util.IOUtil;
import org.dolphin.secret.browser.CacheManager;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class ReadableFileInputStream extends InputStream {
    public static final String TAG = "ReadableFileInputStream";
    private final File file;
    private FileInfo fileInfo = null;
    private long nextReadIndex = 0;
    private FileInfoContentCache contentCache;
    private RandomAccessFile randomAccessFile;
    private int markLimit = 0;

    public ReadableFileInputStream(File file) throws Throwable {
        this.file = file;
        randomAccessFile = new RandomAccessFile(file, "r");
        this.fileInfo = getFileInfo();
    }

    public ReadableFileInputStream(File file, FileInfo fileInfo) throws FileNotFoundException {
        if (null == fileInfo) {
            throw new NullPointerException("");
        }
        this.file = file;
        this.fileInfo = fileInfo;
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    public ReadableFileInputStream(File file, FileInfo fileInfo, FileInfoContentCache cache) throws FileNotFoundException {
        this.file = file;
        this.fileInfo = fileInfo;
        this.contentCache = cache;
        randomAccessFile = new RandomAccessFile(file, "r");
    }

    @Override
    public int read() throws IOException {
        try {
            FileInfo fileInfo = getFileInfo();
            if (nextReadIndex >= fileInfo.originalFileLength) {
                return -1;
            }

            int transferSize = fileInfo.transferSize;
            if (nextReadIndex < transferSize) {
                // 需要从加密过的头部读取信息
                FileInfoContentCache cache = getContentCache();
                int index = (int) (nextReadIndex++);
                return 255 & cache.headBodyContent[index];
            }

            if (nextReadIndex >= fileInfo.originalFileLength - transferSize) {
                // 需要从加密过的尾部读取信息
                FileInfoContentCache cache = getContentCache();
                int index = (int) (nextReadIndex++ - (fileInfo.originalFileLength - transferSize));
                return 255 & cache.footBodyContent[index];
            }
            // 从中间读取
            if (nextReadIndex == transferSize) { // 正在起始的边界位置
                randomAccessFile.seek(nextReadIndex);
            }
            int res = randomAccessFile.read();
            nextReadIndex++;
            return res;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(throwable);
        } finally {

        }
    }

    @Override
    public int read(byte[] buffer) throws IOException {
        return read(buffer, 0, buffer.length);
    }

    @Override
    public int read(byte[] buffer, int byteOffset, int byteCount) throws IOException {
        Log.d(TAG, "Current index " + nextReadIndex + " , byteCount " + byteCount);
        int result = 0;
        try {
            FileInfo fileInfo = getFileInfo();
            if (nextReadIndex >= fileInfo.originalFileLength) {
                return -1;
            }
            int arrayLength = buffer.length;
            if ((byteOffset | byteCount) < 0 || byteOffset > arrayLength || arrayLength - byteOffset < byteCount) {
                throw new ArrayIndexOutOfBoundsException();
            }

            long left = nextReadIndex;
            long right = nextReadIndex + byteCount - 1;
            right = right >= fileInfo.originalFileLength ? fileInfo.originalFileLength - 1 : right;
            // 读取的区域包括 [left. right]
            int copyCount = (int) (right - left + 1);
            result = copyCount;
            if (copyCount <= 0) return copyCount;

            if (left < fileInfo.transferSize) { // 头部[0,transferSize)
                FileInfoContentCache cache = getContentCache();
                int maxCopyCount = (int) (fileInfo.transferSize - left);
                maxCopyCount = maxCopyCount > copyCount ? copyCount : maxCopyCount;
                System.arraycopy(cache.headBodyContent, (int) left, buffer, byteOffset, maxCopyCount);
                copyCount -= maxCopyCount;
                byteOffset += maxCopyCount;
                left += maxCopyCount;
            }
            if (copyCount <= 0) return result;

            if (left < fileInfo.originalFileLength - fileInfo.transferSize) { // 读取文件中间的内容
                randomAccessFile.seek(left);
                int maxCopyCount = (int) (fileInfo.originalFileLength - fileInfo.transferSize - left);
                maxCopyCount = maxCopyCount > copyCount ? copyCount : maxCopyCount;
                randomAccessFile.read(buffer, byteOffset, maxCopyCount);
                copyCount -= maxCopyCount;
                byteOffset += maxCopyCount;
                left += maxCopyCount;
            }
            if (copyCount <= 0) return result;

            if (left >= fileInfo.originalFileLength - fileInfo.transferSize) {
                FileInfoContentCache cache = getContentCache();
                System.arraycopy(cache.footBodyContent, (int) (left - (fileInfo.originalFileLength - fileInfo.transferSize)),
                        buffer, byteOffset, copyCount);
            }
            return result;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(throwable);
        } finally {
            nextReadIndex += result;
        }
    }

    @Override
    public int available() throws IOException {
        return super.available();
    }

    @Override
    public void mark(int readlimit) {
        Log.d(TAG, "mark " + readlimit);
        markLimit = readlimit;
    }

    @Override
    public boolean markSupported() {
        return true;
    }

    @Override
    public synchronized void reset() throws IOException {
        nextReadIndex = markLimit;
        randomAccessFile.seek(nextReadIndex);
        Log.d(TAG, "reset " + nextReadIndex);
    }

    @Override
    public long skip(long byteCount) throws IOException {
        Log.d(TAG, "skip " + byteCount);
        try {
            FileInfo fileInfo = getFileInfo();
            long max = fileInfo.originalFileLength - nextReadIndex;
            byteCount = max > byteCount ? byteCount : max;
            nextReadIndex += byteCount;
            randomAccessFile.seek(nextReadIndex);
            return byteCount;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new IOException(throwable);
        }
    }


    @Override
    public void close() throws IOException {
        IOUtil.safeClose(randomAccessFile);
        super.close();
    }

    private FileInfoContentCache getContentCache() throws Throwable {
        if (null == contentCache) {
            contentCache = CacheManager.getInstance().getCache(getFileInfo());
            if (null == contentCache || null == contentCache.footBodyContent || null == contentCache.headBodyContent) {
                contentCache = FileConstants.fillCacheBody(file, getFileInfo(), contentCache);
                CacheManager.getInstance().putCache(getFileInfo(), contentCache);
            }
        }

        return contentCache;
    }

    private FileInfo getFileInfo() throws Throwable {
        if (null == fileInfo) {
            ObscureFileInfoReaderOperator operator = ObscureFileInfoReaderOperator.DEFAULT;
            fileInfo = operator.operate(file);
        }
        return fileInfo;
    }
}
