package org.dolphin.secret.core;

import android.graphics.Bitmap;
import android.support.annotation.NonNull;
import android.util.Log;

import org.dolphin.http.MimeType;
import org.dolphin.job.Operator;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.Preconditions;
import org.dolphin.lib.util.ByteUtil;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.secret.util.BitmapUtils;
import org.dolphin.secret.util.UnsupportEncode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Created by hanyanan on 2016/1/15.
 * 混淆指定的文件
 * 加密算法版本号1（1默认）：局部加密，只是加密头部和尾部，适用于大文件格式：
 * ****************************|*******************************************|************************<br>
 * SECRET CALCULATOR           | 文件的格式(char)                          | 32个字节<br>
 * ****************************|*******************************************|************************<br>
 * 1                           | 程序的版本号(int)                         | 4字节<br>
 * ****************************|*******************************************|************************<br>
 * 1                           | 加密算法的版本号(int)                     | 4字节<br>
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
 * 218ud()_(09)0               | 乱码，移动的块大小-上面的头部大小         | (移动的块大小-上面的大小)字节<br>
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
 * 1                           | 程序的版本号(int)                         | 4字节<br>
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
 */
public class ObscureOperator implements Operator<File, TwoTuple<ObscureFileInfo, FileInfoContentCache>> {
    public static final String TAG = "ObscureOperator";
    public static final ObscureOperator INSTANCE = new ObscureOperator();
    private final Random RANDOM = new Random();

    public static boolean shouldEncodeBigMode(File input, int transferSize) {
        long originalFileLength = input.length();
        return (originalFileLength > transferSize * 4 && originalFileLength > 16 * 1024);
    }

    /**
     * 混淆大文件，一般只是混淆头部和尾部，encodeversion为2
     */
    private static boolean proguardSmallFile(File originalFile, ObscureFileInfo fileInfo, FileInfoContentCache cache) throws Throwable {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RandomAccessFile randomAccessFile = null;
        boolean success = false;
        boolean modify = false;
        byte[] body = null;
        int thumbnailRangeOffset = 0;
        int thumbnailRangeCount = 0;
        cache.footBodyContent = null;
        int originalFileLength = (int) originalFile.length();
        cache.thumbnail = getThumbnail(originalFile, fileInfo);
        try {
            randomAccessFile = new RandomAccessFile(originalFile, "rw");
            byte[] dom = new byte[32];
            randomAccessFile.read(dom);
            randomAccessFile.seek(0);
            fileVerify(dom);
            fileInfo.encodeVersion = 2;
            fileInfo.transferSize = originalFileLength;
            outputStream.write(FileConstants.getFileDom()); // 文件的格式
            outputStream.write(FileConstants.getSoftwareVersion()); // 程序的版本号
            outputStream.write(ByteUtil.intToBytes(fileInfo.encodeVersion)); // 加密算法的版本号
            outputStream.write(FileConstants.getRandomBoundByte(16)); // 随即填充字符
            outputStream.write(ByteUtil.longToBytes(fileInfo.originalFileLength)); // 原始文件长度
            outputStream.write(ByteUtil.intToBytes(fileInfo.originalFileName.getBytes().length)); // 原是文件名长度
            outputStream.write(fileInfo.originalFileName.getBytes()); // 原始文件名
            outputStream.write(ByteUtil.longToBytes(fileInfo.originalModifyTimeStamp)); // 原始文件修改时间
            outputStream.write(FileConstants.getMimeType(fileInfo.originalFileName)); // 原始文件类型
            outputStream.write(ByteUtil.intToBytes(fileInfo.transferSize)); // transferSize

            if (null == cache.thumbnail) {
                outputStream.write(ByteUtil.intToBytes(0)); // thumbnail大小
            } else {
                ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
                cache.thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bitmapOutputStream);
                byte[] bm = bitmapOutputStream.toByteArray();
                outputStream.write(ByteUtil.intToBytes(bm.length)); // thumbnail大小
                thumbnailRangeOffset = outputStream.size();
                outputStream.write(bm); // thumbnail大小
                thumbnailRangeCount = bm.length;
            }
            outputStream.write(ByteUtil.longToBytes(fileInfo.encodeTime)); // 加密时间戳
            outputStream.write(getExtraMessage(originalFile, fileInfo));
            body = new byte[originalFileLength];
            randomAccessFile.readFully(body);
            outputStream.write(FileConstants.encode(body));
            modify = true;
            randomAccessFile.seek(0);
            randomAccessFile.setLength(outputStream.size());
            randomAccessFile.write(outputStream.toByteArray());
            success = true;
            fileInfo.originalFileFooterRange = null;
            fileInfo.originalFileHeaderRange = null;
            if (thumbnailRangeOffset > 0 && thumbnailRangeCount > 0) {
                fileInfo.thumbnailRange = new ObscureFileInfo.Range();
                fileInfo.thumbnailRange.offset = thumbnailRangeOffset;
                fileInfo.thumbnailRange.count = thumbnailRangeCount;
            }
            cache.footBodyContent = body;
        } finally {
            if (!success && modify) { //加密失败，恢复原始文件
                randomAccessFile.seek(fileInfo.originalFileLength);
                randomAccessFile.seek(0);
                randomAccessFile.write(body);
            }
            IOUtil.safeClose(randomAccessFile);
            IOUtil.safeClose(outputStream);
        }
        return success;
    }

    /**
     * 混淆大文件，一般只是混淆头部和尾部，encodeversion为1
     */
    private static boolean proguardLargeFile(File originalFile, ObscureFileInfo fileInfo, FileInfoContentCache outCache) throws Throwable {
        RandomAccessFile randomAccessFile = null;
        boolean success = false;
        boolean modified = false;
        byte[] originalHeadBuffer = null;
        byte[] originalFootBuffer = null;
        int transferSize = fileInfo.transferSize;
        outCache.thumbnail = getThumbnail(originalFile, fileInfo);
        try {
            randomAccessFile = new RandomAccessFile(originalFile, "rw");
            byte[] dom = new byte[32];
            randomAccessFile.read(dom);
            fileVerify(dom);
            randomAccessFile.seek(0);
            originalHeadBuffer = new byte[transferSize];
            originalFootBuffer = new byte[transferSize];
            randomAccessFile.readFully(originalHeadBuffer);
            randomAccessFile.seek(fileInfo.originalFileLength - transferSize);
            randomAccessFile.readFully(originalFootBuffer);
            randomAccessFile.seek(0);
            outCache.footBodyContent = originalFootBuffer;
            outCache.headBodyContent = originalHeadBuffer;
            modified = true;
            writeProguardHeader(randomAccessFile, fileInfo, transferSize);
            obscureOriginalTailAndHeader(randomAccessFile, fileInfo, originalHeadBuffer, originalFootBuffer);
            writeProguardFooter(randomAccessFile, originalFile, fileInfo, outCache);
            success = true;
        } finally {
            if (!success && modified) {
                // 加密出现异常，并且文件已经被修改过。需要恢复以前的初始态
                randomAccessFile.seek(0);
                randomAccessFile.write(originalHeadBuffer);
                randomAccessFile.seek(fileInfo.originalFileLength - transferSize);
                randomAccessFile.write(originalFootBuffer);
                randomAccessFile.setLength(fileInfo.originalFileLength);
            }
            IOUtil.safeClose(randomAccessFile);
        }

        return success;
    }

    /**
     * 检查是否是支持加密，就是查看文件头部的32个字节是否是{@link FileConstants#FILE_DOM}
     *
     * @param dom
     * @return
     */
    private static void fileVerify(byte[] dom) throws UnsupportEncode {
        byte[] encodeFileDom = FileConstants.getFileDom();
        Preconditions.checkArgument(dom != null && encodeFileDom != null);
        if (dom.length != encodeFileDom.length) {
            throw new UnsupportEncode();
        }
        int length = dom.length;
        for (int i = 0; i < length; ++i) {
            if (dom[i] != encodeFileDom[i]) return;
        }
        throw new UnsupportEncode();
    }

    // 恒定大小为1024
    public static byte[] getExtraMessage(File file, ObscureFileInfo fileInfo) {
        byte[] res = new byte[FileConstants.EXTRA_MESSAGE_SIZE];
        return res;
    }

    /**
     * 创建当前文件的thumbnail
     */
    public static Bitmap getThumbnail(File file, ObscureFileInfo fileInfo) {
//        String mime = fileInfo.originalMimeType;
//        try {
//            if (mime.startsWith("image")) {
//                return createImageThumbnail(file);
//            }
//            if (mime.startsWith("video")) {
//                return ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
//            }
//        } catch (IOException exception) {
//
//        }
//        // TODO
//        return null;

        String mime = fileInfo.originalMimeType;
        if (mime.startsWith("image")) {
            return BitmapUtils.ImageThumbnailUtils.DEFAULT.extractThumbnail(file.getAbsolutePath(), 200, 200, null);
        }
        if (mime.startsWith("video")) {
            return BitmapUtils.VideoThumbnailUtils.DEFAULT.extractThumbnail(file.getAbsolutePath(), 200, 200, null);
        }
        return null;
    }

    /**
     * 返回当前系统时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 对原始的头部信息和尾部信息加密，尾部的[originalLength - transferSize, originalLength)加密后重新写入，
     * 将头部[0-transferSize)的内容加密后[originalLength, originalLength+transferSize)
     */
    private static void obscureOriginalTailAndHeader(RandomAccessFile randomAccessFile, ObscureFileInfo fileInfo,
                                                     byte[] originalHeadData, byte[] originalFootData) throws IOException {
        Preconditions.checkNotNulls(originalHeadData, originalFootData);
        Preconditions.checkArgument(originalHeadData.length > 0);
        Preconditions.checkArgument(originalFootData.length > 0);
        Preconditions.checkArgument(originalFootData.length == originalHeadData.length);
        Preconditions.checkArgument(originalFootData.length == fileInfo.transferSize);
        randomAccessFile.seek(fileInfo.originalFileLength - fileInfo.transferSize);
        randomAccessFile.write(FileConstants.encode(originalFootData));
        randomAccessFile.write(FileConstants.encode(originalHeadData));
        ObscureFileInfo.Range footRange = new ObscureFileInfo.Range();
        footRange.offset = fileInfo.originalFileLength - fileInfo.transferSize;
        footRange.count = fileInfo.transferSize;
        fileInfo.originalFileFooterRange = footRange;

        ObscureFileInfo.Range headRange = new ObscureFileInfo.Range();
        headRange.offset = fileInfo.originalFileLength;
        headRange.count = fileInfo.transferSize;
        fileInfo.originalFileHeaderRange = headRange;
    }

    /**
     * 向文件的末尾写入一些额外的信息
     * 包括额外的信息，加密时间，thumbnail
     *
     * @param file
     * @param fileInfo
     */
    private static void writeProguardFooter(RandomAccessFile randomAccessFile, File file,
                                            ObscureFileInfo fileInfo, FileInfoContentCache cache) throws IOException {
        randomAccessFile.seek(fileInfo.originalFileLength + fileInfo.transferSize);
        fileInfo.extraTag = getExtraMessage(file, fileInfo);// 额外的信息，1024字节
        randomAccessFile.write(fileInfo.extraTag);
        randomAccessFile.write(ByteUtil.longToBytes(fileInfo.encodeTime)); // 写入加密时间，8字节
        if (null == cache.thumbnail) {
            randomAccessFile.write(ByteUtil.intToBytes(0));
        } else {
            ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
            cache.thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bitmapOutputStream);
            byte[] bm = bitmapOutputStream.toByteArray();
            randomAccessFile.write(bm);
            randomAccessFile.write(ByteUtil.intToBytes(bm.length));
            ObscureFileInfo.Range range = new ObscureFileInfo.Range();
            range.count = bm.length;
            range.offset = fileInfo.originalFileLength + fileInfo.transferSize + fileInfo.extraTag.length + 8;
            fileInfo.thumbnailRange = range;
        }
    }

    /**
     * 向文件中写入格式化的头部信息和包括头部bound的随机段，总共大小就是指定的移动区域。
     * 写入的区域包括[0-transferSize)。
     *
     * @param file         需要写入的文件
     * @param fileInfo     该文件的基本信息
     * @param transferSize 该文件计算出来的需要移动的大小。
     * @throws IOException
     */
    private static void writeProguardHeader(RandomAccessFile file, ObscureFileInfo fileInfo, int transferSize) throws IOException {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        outputStream.write(FileConstants.getFileDom()); // 文件的格式
        outputStream.write(FileConstants.getSoftwareVersion()); // 程序的版本号
        outputStream.write(FileConstants.getEncodeVersion()); // 加密算法的版本号
        outputStream.write(FileConstants.getRandomBoundByte(16)); // 随即填充字符
        outputStream.write(ByteUtil.longToBytes(fileInfo.originalFileLength)); // 原始文件长度
        outputStream.write(ByteUtil.intToBytes(fileInfo.originalFileName.getBytes().length)); // 原是文件名长度
        outputStream.write(fileInfo.originalFileName.getBytes()); // 原始文件名
        outputStream.write(ByteUtil.longToBytes(fileInfo.originalModifyTimeStamp)); // 原始文件修改时间
        outputStream.write(FileConstants.getMimeType(fileInfo.originalFileName)); // 原始文件类型
        outputStream.write(ByteUtil.intToBytes(transferSize)); // 移动的块大小
        outputStream.flush();
        int size = outputStream.size();
        file.write(outputStream.toByteArray());
        file.write(FileConstants.getRandomBoundByte(transferSize - size));
    }

    /**
     * 从fileInfo中计算出需要混淆的头部大小,计算方式为基本的长度+文件名称序列化后的大小
     *
     * @param fileInfo 需要计算的文件信息集合
     * @return
     */
    public static int calculateProguardHeaderSize(ObscureFileInfo fileInfo) {
        return 32 // 文件的格式
                + 4 // 程序的版本号
                + 4 // 加密算法的版本号
                + 16 // 随即填充字符
                + 8 // 原始文件长度
                + 4 // 原是文件名长度
                + fileInfo.originalFileName.getBytes().length // 原始文件名
                + 8 // 原始文件修改时间
                + 32 // 原始文件类型
                + 4 // 移动的块大小(byte, 1024 * 2 * X)
                ;
    }

    /**
     * @param fileInfo
     * @return
     */
    public static int calculateTransferSize(ObscureFileInfo fileInfo) {
        int proguardHeaderSize = calculateProguardHeaderSize(fileInfo);
        int pages = proguardHeaderSize / FileConstants.TRANSFER_PAGE_SIZE + 1;
        return pages * FileConstants.TRANSFER_PAGE_SIZE;
    }

    /**
     * 创建一个基本的文件信息, 包括如下字段：
     * 文件的格式(char)
     * 程序的版本号(int)
     * 加密算法的版本号(int)
     * 原始文件长度(long)
     * 原始文件名(char)
     * 原始文件修改时间(long)
     * 原始文件类型(char)
     */
    private static ObscureFileInfo createFileInfo(File file) {
        ObscureFileInfo fileInfo = new ObscureFileInfo();
        fileInfo.dom = new String(FileConstants.getFileDom());
        fileInfo.softwareVersion = ByteUtil.bytesToInt(FileConstants.getSoftwareVersion());
        fileInfo.encodeVersion = ByteUtil.bytesToInt(FileConstants.getEncodeVersion());
        fileInfo.originalFileLength = file.length();
        fileInfo.originalModifyTimeStamp = file.lastModified();
        fileInfo.originalFileName = file.getName();
        fileInfo.originalMimeType = MimeType.createFromFileName(file.getName()).getMimeType();
        return fileInfo;
    }

    // 单项，不可逆
    public static String createProguardFileName(String originalFileName) {
        return String.valueOf(System.currentTimeMillis());
    }

    private int getRandomSuffix() {
        return Math.abs(RANDOM.nextInt()) / 1000000;
    }

    /**
     * 加密文件，并返回加密后的文件信息
     *
     * @param input 接受的输入参数
     * @return 加密后的文件信息和头尾&thumbnail信息
     * @throws Throwable
     */
    @NonNull
    public TwoTuple<ObscureFileInfo, FileInfoContentCache> operate(File input) throws Throwable {
        if (!input.isFile() || !input.exists() || !input.canRead() || !input.canWrite()) {
            throw new IOException("File " + input.getAbsolutePath() + " is Not support read or write or both!");
        }
        boolean success;
        FileInfoContentCache cache = new FileInfoContentCache();
        ObscureFileInfo baseFileInfo = createFileInfo(input);
        int transferSize = calculateTransferSize(baseFileInfo); // 2048 * N
        baseFileInfo.transferSize = transferSize;
        baseFileInfo.encodeTime = FileConstants.getCurrentTime();
        try {
            if (shouldEncodeBigMode(input, transferSize)) { //大文件模式，混淆局部
                success = proguardLargeFile(input, baseFileInfo, cache);
            } else { // 全局加密，将信息保存在头部，实际的信息保存在尾部
                success = proguardSmallFile(input, baseFileInfo, cache);
            }
        } catch (Throwable throwable) {
            Log.e(TAG, "Encode file " + input.getName() + " Failed!");
            // release cache
            BitmapUtils.recycle(cache.thumbnail);
            throw throwable;
        }

        if (!success) {
            throw new IOException("File " + input.getAbsolutePath() + " Obscure Failed!");
        }

        // 修改文件名称
        String proguardFileName = createProguardFileName(input.getName());
        try {
            File proguardFile = new File(input.getParentFile(), proguardFileName);
            while (proguardFile.exists()) {
                proguardFile = new File(input.getParentFile(), proguardFileName + "_" + getRandomSuffix());
            }
            IOUtil.renameTo(input, proguardFile, true);
            baseFileInfo.obscuredFileName = proguardFile.getName();
        } catch (IOException exception) {
            exception.printStackTrace();
            baseFileInfo.obscuredFileName = input.getName();
        }
        Log.d(TAG, "Encode file " + baseFileInfo);
        return new TwoTuple<ObscureFileInfo, FileInfoContentCache>(baseFileInfo, cache);
    }
}
