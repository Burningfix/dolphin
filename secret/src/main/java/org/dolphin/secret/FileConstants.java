package org.dolphin.secret;

import org.dolphin.http.MimeType;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.MimeTypeMap;
import org.dolphin.lib.Preconditions;

import java.util.Random;

/**
 * Created by hanyanan on 2016/1/15.
 * 文件格式：
 * ****************************|*******************************************|************************
 * SECRET CALCULATOR           | 文件的格式(char)                          | 32个字节
 * ****************************|*******************************************|************************
 * 2134                        | 程序的版本号(int)                         | 4字节
 * ****************************|*******************************************|************************
 * 1234                        | 加密算法的版本号(int)                     | 4字节
 * ****************************|*******************************************|************************
 * ~!@#$%^&                    | 随即填充字符(char)                        | 16字符
 * ****************************|*******************************************|************************
 * 34343434                    | 原始文件长度(long)                        | 8字节
 * ****************************|*******************************************|************************
 * 12343455                    | 原始文件名长度(int)                       | 4字节
 * ****************************|*******************************************|************************
 * 原始文件名                  | 原始文件名(char)                          | N字节
 * ****************************|*******************************************|************************
 * 12312312323123              | 原始文件修改时间(long)                    | 8字节
 * ****************************|*******************************************|************************
 * mimeType                    | 原始文件类型(char)                        | 32字节
 * ****************************|*******************************************|************************
 * 12334345                    | 移动的块大小(byte, 1024 * 2 * X)          | 4字节
 * ****************************|*******************************************|************************
 * 218ud()_(09)0               | 乱码，移动的块大小-上面的头部大小         | (1024 * 2 * X-上面的大小)字节
 * ****************************|*******************************************|************************
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
 * 。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。。
 * ****************************|*******************************************|************************
 * 加密后的末尾                | 有移动的块大小决定大小                    | 1024 * 2 * X
 * ****************************|*******************************************|************************
 * 加密后的头部                | 有移动的块大小决定大小                    | 1024 * 2 * X
 * ****************************|*******************************************|************************
 * width:1234, height:234      | 文件的扩展信息(byte[])                    | 1024
 * ****************************|*******************************************|************************
 * 123123123123                | 文件加密时间戳(long)                      | 8字节
 * ****************************|*******************************************|************************
 * 文件的thumbnail             | thumbnail(byte[])                         | N字节
 * ****************************|*******************************************|************************
 * thumbnail大小               | thumbnail大小(int)                        | 4字节
 * ****************************|*******************************************|************************
 */
public class FileConstants {
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
        Preconditions.checkArgument(data != null && data.length % 2048 == 0, "");
        // TODO
        return data;
    }

    public static byte[] decode(byte[] data) {
        Preconditions.checkArgument(data != null && data.length % 2048 == 0, "");
        // TODO
        return data;
    }

    /**
     * 返回当前系统时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }
}
