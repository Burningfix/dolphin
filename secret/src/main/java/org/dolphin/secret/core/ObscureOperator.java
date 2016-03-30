package org.dolphin.secret.core;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import org.dolphin.http.MimeType;
import org.dolphin.job.Operator;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.Preconditions;
import org.dolphin.secret.util.UnsupportEncode;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Date;

/**
 * Created by hanyanan on 2016/1/15.
 *
 * 混淆指定的文件
 */
public class ObscureOperator implements Operator<File, TwoTuple<FileInfo, FileInfoContentCache>> {
    public static final String TAG = "ObscureOperator";
    public static final ObscureOperator INSTANCE = new ObscureOperator();

    /**
     * 加密文件，并返回加密后的文件信息
     *
     * @param input 接受的输入参数
     * @return 加密后的文件信息和头尾&thumbnail信息
     * @throws Throwable
     */
    public TwoTuple<FileInfo, FileInfoContentCache> operate(File input) throws Throwable {
        boolean success = false;
        FileInfoContentCache cache = new FileInfoContentCache();
        FileInfo baseFileInfo = createFileInfo(input);
        int transferSize = calculateTransferSize(baseFileInfo);
        baseFileInfo.transferSize = transferSize;
        baseFileInfo.encodeTime = FileConstants.getCurrentTime();
        long originalFileLength = input.length();
        try {
            if (originalFileLength > baseFileInfo.transferSize * 4 && originalFileLength > 64 * 1024) { //大文件模式，混淆局部
                success = proguardLargeFile(input, baseFileInfo, cache);
            } else { // 全局加密，将信息保存在头部，实际的信息保存在尾部
                success = proguardSmallFile(input, baseFileInfo, cache);
            }
        } catch (Throwable throwable) {
            Log.e(TAG, "Encode file " + input.getName() + " Failed!");
            // release cache
            if (null != cache.thumbnail) {
                cache.thumbnail.recycle();
            }
            throw throwable;
        }

        if (success) { // 修改文件名称
            String proguardFileName = createProguardFileName(input.getName());
            try {
                IOUtil.renameTo(input, new File(input.getParentFile(), proguardFileName), true);
                baseFileInfo.proguardFileName = proguardFileName;
            } catch (IOException exception) {
                exception.printStackTrace();
                baseFileInfo.proguardFileName = input.getName();
            }

            Log.d(TAG, "Encode file " + baseFileInfo);
        }
        return new TwoTuple<FileInfo, FileInfoContentCache>(baseFileInfo, cache);
    }

    /**
     * 混淆大文件，一般只是混淆头部和尾部，encodeversion为2
     */
    private static boolean proguardSmallFile(File originalFile, FileInfo fileInfo, FileInfoContentCache cache) throws Throwable {
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        RandomAccessFile randomAccessFile = null;
        boolean success = false;
        boolean modify = false;
        byte[] body = null;
        int thumbnailRangeOffset = 0;
        int thumbnailRangeCount = 0;
        cache.footBodyContent = null;
        int originalFileLength = (int) originalFile.length();
        cache.thumbnail = createBitmap(originalFile, fileInfo);
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
            thumbnailRangeOffset = outputStream.size();
            if (null == cache.thumbnail) {
                outputStream.write(ByteUtil.intToBytes(0)); // thumbnail大小
            } else {
                ByteArrayOutputStream bitmapOutputStream = new ByteArrayOutputStream();
                cache.thumbnail.compress(Bitmap.CompressFormat.PNG, 100, bitmapOutputStream);
                byte[] bm = bitmapOutputStream.toByteArray();
                outputStream.write(ByteUtil.intToBytes(bm.length)); // thumbnail大小
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
                fileInfo.thumbnailRange = new FileInfo.Range();
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
    private static boolean proguardLargeFile(File originalFile, FileInfo fileInfo, FileInfoContentCache cache) throws Throwable {
        RandomAccessFile randomAccessFile = null;
        boolean success = false;
        boolean modified = false;
        byte[] originalHeadBuffer = null;
        byte[] originalFootBuffer = null;
        int transferSize = fileInfo.transferSize;
        cache.thumbnail = createBitmap(originalFile, fileInfo);
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
            modified = true;
            writeProguardHeader(randomAccessFile, fileInfo, transferSize);
            proguardOriginalTail(randomAccessFile, fileInfo, originalHeadBuffer, originalFootBuffer);
            writeProguardFooter(randomAccessFile, originalFile, fileInfo, cache);
            cache.footBodyContent = originalFootBuffer;
            cache.headBodyContent = originalHeadBuffer;
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
    public static byte[] getExtraMessage(File file, FileInfo fileInfo) {
        byte[] res = new byte[FileConstants.EXTRA_MESSAGE_SIZE];
        return res;
    }

    /**
     * 创建当前文件的thumbnail
     */
    public static Bitmap createBitmap(File file, FileInfo fileInfo) {
        String mime = fileInfo.originalMimeType;
        try {
            if (mime.startsWith("image")) {
                return createImageThumbnail(file, fileInfo);
            }
            if (mime.startsWith("video")) {
                return ThumbnailUtils.createVideoThumbnail(file.getPath(), MediaStore.Video.Thumbnails.MINI_KIND);
            }
        } catch (IOException exception) {

        }
        // TODO
        return null;
    }

    // 尽量的靠近200*200
    public static Bitmap createImageThumbnail(File file, FileInfo fileInfo) throws IOException {
        FileInputStream inputStream = null;
        try {
            inputStream = new FileInputStream(file);
//            inputStream.mark(0);
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            BitmapFactory.decodeStream(inputStream, null, options);
            int w = options.outWidth;
            int h = options.outHeight;
            options.inJustDecodeBounds = false;
            options.inSampleSize = FileConstants.calculateSampleSize(w, h, 200, 200);
//            inputStream.reset();
            IOUtil.safeClose(inputStream);
            inputStream = new FileInputStream(file);
            Bitmap bitmap = BitmapFactory.decodeStream(inputStream, null, options);
            return bitmap;
        } finally {
            IOUtil.safeClose(inputStream);
        }
    }

    // 尽量的靠近200*200
    public static Bitmap createAudioThumbnail(String path) {
        return null;
    }


    /**
     * 返回当前系统时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 对原始的头部信息和尾部信息加密，尾部的[originalLength - transferSize, originalLength)机密后重新写入，
     * 将头部[0-transferSize)的内容加密后[originalLength, originalLength+transferSize)
     */
    private static void proguardOriginalTail(RandomAccessFile randomAccessFile, FileInfo fileInfo,
                                             byte[] originalHeadData, byte[] originalFootData) throws IOException {
        Preconditions.checkNotNulls(originalHeadData, originalFootData);
        Preconditions.checkArgument(originalHeadData.length > 0);
        Preconditions.checkArgument(originalFootData.length > 0);
        Preconditions.checkArgument(originalFootData.length == originalHeadData.length);
        Preconditions.checkArgument(originalFootData.length == fileInfo.transferSize);
        randomAccessFile.seek(fileInfo.originalFileLength - fileInfo.transferSize);
        randomAccessFile.write(FileConstants.encode(originalFootData));
        randomAccessFile.write(FileConstants.encode(originalHeadData));
        FileInfo.Range footRange = new FileInfo.Range();
        footRange.offset = fileInfo.originalFileLength - fileInfo.transferSize;
        footRange.count = fileInfo.transferSize;
        fileInfo.originalFileFooterRange = footRange;

        FileInfo.Range headRange = new FileInfo.Range();
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
    private static void writeProguardFooter(RandomAccessFile randomAccessFile, File file, FileInfo fileInfo,
                                            FileInfoContentCache cache) throws IOException {
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
            FileInfo.Range range = new FileInfo.Range();
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
    private static void writeProguardHeader(RandomAccessFile file, FileInfo fileInfo, int transferSize) throws IOException {
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
     * 从fileInfo中计算出需要混淆的头部大小
     *
     * @param fileInfo 需要计算的文件信息集合
     * @return
     */
    public static int calculateProguardHeaderSize(FileInfo fileInfo) {
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
    public static int calculateTransferSize(FileInfo fileInfo) {
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
    private static FileInfo createFileInfo(File file) {
        FileInfo fileInfo = new FileInfo();
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
        Date date = new Date();

        return String.valueOf(System.currentTimeMillis());
    }
}
