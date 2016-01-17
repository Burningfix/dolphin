package org.dolphin.secret.coder;

import android.graphics.Bitmap;
import android.os.SystemClock;

import org.dolphin.http.MimeType;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.MimeTypeMap;
import org.dolphin.secret.FileConstants;
import org.dolphin.secret.FileInfo;
import org.dolphin.secret.util.ByteBuffer;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class FileEncoder {
    private final File originalFile;
    private final boolean deleteOldFile;

    public FileEncoder(File file, boolean deleteOldFile) {
        originalFile = file;
        this.deleteOldFile = deleteOldFile;
    }

    /**
     * 检查是否是支持加密，就是查看文件头部的32个字节是否是{@link org.dolphin.secret.FileConstants#FILE_DOM}
     *
     * @param dom
     * @return
     */
    private static void fileVerify(byte[] dom) throws UnsupportEncode {
//        byte[] encodeFileDom = FileConstants.getFileDom();
//        if (dom == null || encodeFileDom == null) return false;
//        if (dom.length != encodeFileDom.length) return true;
//        int length = dom.length;
//        for (int i = 0; i < length; ++i) {
//            if (dom[i] != encodeFileDom[i]) return false;
//        }
//        return true;
    }

    public void encode() throws UnsupportEncode, IOException {
        RandomAccessFile randomAccessFile = null;
        boolean success = false;
        boolean modified = false;
        byte[] originalHeadBuffer = null;
        byte[] originalFootBuffer = null;
        final int PageSize = FileConstants.TRANSFER_PAGE_SIZE;
        try {
            randomAccessFile = new RandomAccessFile(originalFile, "rw");
            byte[] dom = new byte[32];
            randomAccessFile.read(dom);
            fileVerify(dom);
            FileInfo baseFileInfo = createFileInfo(originalFile);
            int transferSize = calculateTransferSize(baseFileInfo);
            randomAccessFile.seek(0);
            originalHeadBuffer = new byte[transferSize];
            originalFootBuffer = new byte[transferSize];
            randomAccessFile.readFully(originalHeadBuffer);
            randomAccessFile.seek(baseFileInfo.originalFileLength - transferSize);
            randomAccessFile.readFully(originalFootBuffer);
            randomAccessFile.seek(0);
            modified = true;
            writeProguardHeader(randomAccessFile, baseFileInfo, transferSize);
            randomAccessFile.seek(baseFileInfo.originalFileLength - transferSize);
            randomAccessFile.write(FileConstants.encode(originalFootBuffer));
            randomAccessFile.write(FileConstants.encode(originalHeadBuffer));

        } finally {
            if (!success && !modified) {

            }

            IOUtil.safeClose(randomAccessFile);
        }
    }

    // 恒定大小为1024
    public static byte[] getExtraMessage(File file, FileInfo fileInfo) {
        int size = FileConstants.EXTRA_MESSAGE_SIZE;
        byte[] res = new byte[size];

        return res;
    }

    /**
     * 创建当前文件的thumbnail
     */
    public Bitmap createBitmap() {
        return null;
    }

    /**
     * 返回当前系统时间
     */
    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * 向文件的末尾写入一些额外的信息
     *
     * @param file
     * @param fileInfo
     * @param transferSize
     */
    private static void writeProguardFooter(RandomAccessFile randomAccessFile, File file, FileInfo fileInfo, int transferSize) throws IOException {
        randomAccessFile.write(getExtraMessage(file, fileInfo));
        randomAccessFile.write(ByteUtil.longToBytes(getCurrentTime()));
        Bitmap thumbnail = fileInfo.thumbnail;
        if(null == thumbnail) {
            randomAccessFile.write(ByteUtil.intToBytes(0));
        }else{
            thumbnail.
            randomAccessFile.write(ByteUtil.intToBytes(0));
            randomAccessFile.write(ByteUtil.intToBytes(0));
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
        int pages = proguardHeaderSize / 4 + 1;
        return pages * 2048;
    }

    /**
     * 创建一个基本的文件信息
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
}
