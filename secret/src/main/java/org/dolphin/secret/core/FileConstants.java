package org.dolphin.secret.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.util.Log;

import org.dolphin.http.MimeType;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.Preconditions;
import org.dolphin.lib.ValueUtil;
import org.dolphin.secret.browser.CacheManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Created by hanyanan on 2016/1/15.
 * 加密算法版本号1（1默认）：局部加密，只是加密头部和尾部，适用于大文件格式：
 * ****************************|*******************************************|************************<br>
 * SECRET CALCULATOR           | 文件的格式(char)                          | 32个字节<br>
 * ****************************|*******************************************|************************<br>
 * 1                           | 程序的版本号(int)                         | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 1234                        | 加密算法的版本号(int)                     | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * ~!@#$%^&                    | 随即填充字符(char)                        | 16字符<br>
 * ****************************|*******************************************|************************<br>
 * 34343434                    | 原始文件长度(long)                        | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * 12343455                    | 原始文件名长度(int)                       | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 原始文件名                  | 原始文件名(char)                          | N字节<br>
 * ****************************|*******************************************|************************<br>
 * 12312312323123              | 原始文件修改时间(long)                    | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * mimeType                    | 原始文件类型(char)                        | 32字节<br>
 * ****************************|*******************************************|************************<br>
 * 12334345                    | 移动的块大小(byte, 1024 * 2 * X)          | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 218ud()_(09)0               | 乱码，移动的块大小-上面的头部大小         | (1024 * 2 * X-上面的大小)字节<br>
 * ****************************|*******************************************|************************<br>
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。<br>
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。<br>
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。<br>
 * ****************************|*******************************************|************************<br>
 * 加密后的末尾                | 有移动的块大小决定大小                    | 1024 * 2 * X(加密版本版本为1)<br>
 * ****************************|*******************************************|************************<br>
 * 加密后的头部                | 有移动的块大小决定大小                    | 1024 * 2 * X(加密版本版本为1)<br>
 * ****************************|*******************************************|************************<br>
 * width:1234, height:234      | 文件的扩展信息(byte[])                    | 1024<br>
 * ****************************|*******************************************|************************<br>
 * 123123123123                | 文件加密时间戳(long)                      | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * 文件的thumbnail             | thumbnail(byte[])                         | N字节<br>
 * ****************************|*******************************************|************************<br>
 * thumbnail大小               | thumbnail大小(int)                        | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * <br>
 * <br>
 * 2：全局加密，加密整个文件，适用于小文件和一些文本文件<br>
 * ****************************|*******************************************|************************<br>
 * SECRET CALCULATOR           | 文件的格式(char)                          | 32个字节<br>
 * ****************************|*******************************************|************************<br>
 * 2134                        | 程序的版本号(int)                         | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 2                           | 加密算法的版本号(int)                     | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * ~!@#$%^&                    | 随即填充字符(char)                        | 16字符<br>
 * ****************************|*******************************************|************************<br>
 * 34343434                    | 原始文件长度(long)                        | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * 12343455                    | 原始文件名长度(int)                       | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 原始文件名                  | 原始文件名(char)                          | N字节<br>
 * ****************************|*******************************************|************************<br>
 * 12312312323123              | 原始文件修改时间(long)                    | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * mimeType                    | 原始文件类型(char)                        | 32字节<br>
 * ****************************|*******************************************|************************<br>
 * 12334345                    | 移动的块大小(byte, 1024 * 2 * X)          | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 12123                       | thumbnail大小                             | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 432442144                   | thumbnail                                 |N字节<br>
 * ****************************|*******************************************|************************<br>
 * 123123123123                | 文件加密时间戳(long)                      | 8字节<br>
 * ****************************|*******************************************|************************<br>
 * width:1234, height:234      | 文件的扩展信息(byte[])                    | 1024<br>
 * ****************************|*******************************************|************************<br>
 * ......................................(加密后的文件).............................................<br>
 * 前面64个字符是通用的，表示文件dom，软件版本，加密算法版本，原始文件长度
 */
public class FileConstants {
    public static final String TAG = "FileConstants";
    public static final Random RANDOM = new Random();
    public static final byte[] FILE_DOM = "SECRET CALCULATOR".getBytes(); // 32字节
    public static final byte[] SOFTWARE_VERSION = ByteUtil.intToBytes(1); // 软件版本， 4字节
    public static final byte[] ENCODE_VERSION = ByteUtil.intToBytes(1); // 软件加密版本，4字节
    public static final int TRANSFER_PAGE_SIZE = 2048;
    public static final int EXTRA_MESSAGE_SIZE = 1024;

    // 文件dom,32字节
    public static byte[] getFileDom() {
        byte[] res = new byte[32];
        System.arraycopy(FILE_DOM, 0, res, 0, FILE_DOM.length);
        return res;
    }

    // 软件版本， 4字节
    public static byte[] getSoftwareVersion() {
        return SOFTWARE_VERSION;
    }

    // 软件加密版本，4字节
    public static byte[] getEncodeVersion() {
        return ENCODE_VERSION;
    }

    // 随机填充字符， 16字节
    public static byte[] getRandomBoundByte(int size) {
        byte[] res = new byte[size];
        RANDOM.nextBytes(res);
        return res;
    }

    // 原始文件类型, 32字节
    public static byte[] getMimeType(String fileName) {
        byte[] res = new byte[32];
        String originalMimeType = MimeType.createFromFileName(fileName).getMimeType();
        byte[] data = originalMimeType.getBytes();
        System.arraycopy(data, 0, res, 0, data.length);
        return res;
    }

    public static byte[] encode(byte[] data) {
        //Preconditions.checkArgument(data != null && data.length % 2048 == 0, "");
        // TODO
        return data;
    }

    public static byte[] decode(byte[] data) {
        //Preconditions.checkArgument(data != null && data.length % 2048 == 0, "");
        // TODO
        return data;
    }

    public static FileInfoContentCache createContentCache(File file, FileInfo fileInfo) throws IOException {
        FileInfoContentCache cache = new FileInfoContentCache();
        if (fileInfo.encodeVersion == 1) {
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
        } else {
            RandomAccessFile randomAccessFile = null;
            try {
                randomAccessFile = new RandomAccessFile(file, "r");
                randomAccessFile.seek(file.length() - fileInfo.originalFileLength);
                byte[] body = new byte[(int) fileInfo.originalFileLength];
                randomAccessFile.readFully(body);
                cache.headBodyContent = FileConstants.decode(body);
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }
        }
        return cache;
    }

    public static Bitmap readThumbnailFromEncodedFile(File file, FileInfo fileInfo) throws IOException {
        if (null == fileInfo.thumbnailRange || fileInfo.thumbnailRange.count <= 0) {
            Log.i(TAG, "fileInfo.thumbnailRange == null or fileInfo.thumbnailRange.count == 0!");
            return null;
        }
        FileInfoContentCache cache = CacheManager.getInstance().getCache(fileInfo);
        if (null == cache) {
            cache = createContentCache(file, fileInfo);
        }
        if (null != cache.thumbnail) {
            return cache.thumbnail;
        }
        RandomAccessFile randomAccessFile = null;
        try {
            Log.d(TAG, "Decode thumbnail for " + file.getName());
            randomAccessFile = new RandomAccessFile(file, "r");
            randomAccessFile.seek(fileInfo.thumbnailRange.offset);
            byte[] buff = new byte[fileInfo.thumbnailRange.count];
            randomAccessFile.readFully(buff);
            Bitmap bm = BitmapFactory.decodeByteArray(buff, 0, buff.length);
            cache.thumbnail = bm;
            CacheManager.getInstance().putCache(fileInfo, cache);
            return bm;
        } finally {
            IOUtil.closeQuietly(randomAccessFile);
        }
    }


    public static int calculateSampleSize(int originalWidth, int originalHeight, int exceptionWidth, int exceptionHeight) {
        int inSample = 1;
        while (originalWidth > exceptionWidth || originalHeight > exceptionHeight) {
            inSample *= 2;
            originalWidth /= 2;
            originalHeight /= 2;
        }
        return inSample;
    }

    /**
     * 返回当前系统时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }


    public static long readLong(RandomAccessFile randomAccessFile, long offset) throws IOException {
        if (offset >= 0) {
            randomAccessFile.seek(offset);
        }
        byte[] buff = new byte[8];
        randomAccessFile.readFully(buff);
        return ByteUtil.bytesToLong(buff);
    }

    public static String readString(RandomAccessFile randomAccessFile, long offset, int count) throws IOException {
        if (offset >= 0) {
            randomAccessFile.seek(offset);
        }
        byte[] buff = new byte[count];
        randomAccessFile.readFully(buff);
        return ByteUtil.bytesToString(buff);
    }

    public static int readInt(RandomAccessFile randomAccessFile, long offset) throws IOException {
        if (offset >= 0) {
            randomAccessFile.seek(offset);
        }
        byte[] buff = new byte[4];
        randomAccessFile.readFully(buff);
        return ByteUtil.bytesToInt(buff);
    }

    public static byte[] readBytes(RandomAccessFile randomAccessFile, long offset, int count) throws IOException {
        if (offset >= 0) {
            randomAccessFile.seek(offset);
        }
        byte[] buff = new byte[count];
        randomAccessFile.readFully(buff);
        return buff;
    }
}
