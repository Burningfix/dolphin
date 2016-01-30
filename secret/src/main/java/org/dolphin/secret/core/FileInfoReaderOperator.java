package org.dolphin.secret.core;

import org.dolphin.job.Operator;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.Preconditions;
import org.dolphin.secret.util.UnsupportEncode;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class FileInfoReaderOperator implements Operator<File, FileInfo> {
    public static final FileInfoReaderOperator DEFAULT = new FileInfoReaderOperator();

    @Override
    public FileInfo operate(File input) throws Throwable {
        FileInfo fileInfo = new FileInfo();
        RandomAccessFile randomAccessFile = null;
        try {
            randomAccessFile = new RandomAccessFile(input, "r");
            fileVerify(randomAccessFile);
            fileInfo.proguardFileName = input.getName();
            readFirst64Bytes(randomAccessFile, fileInfo);
            readOriginalFileName(randomAccessFile, fileInfo);
            readExtraHeadInfo(randomAccessFile, fileInfo);
            if (fileInfo.encodeVersion == 1) {
                readOriginalHeaderAndFooterRange(randomAccessFile, fileInfo);
                readThumbnailRange(randomAccessFile, fileInfo);
                readExtraMessage(randomAccessFile, fileInfo);
            } else {
                long currPosition = randomAccessFile.getFilePointer();
                int thumbnailSize = FileConstants.readInt(randomAccessFile, -1);
                if (thumbnailSize > 0) {
                    fileInfo.thumbnailRange = new FileInfo.Range();
                    fileInfo.thumbnailRange.count = thumbnailSize;
                    fileInfo.thumbnailRange.offset = currPosition;
                    randomAccessFile.skipBytes(thumbnailSize);
                }
                fileInfo.encodeTime = FileConstants.readLong(randomAccessFile, -1);
                fileInfo.extraTag = FileConstants.readBytes(randomAccessFile, -1, 1024);
            }
        } finally {
            IOUtil.safeClose(randomAccessFile);
        }

        return fileInfo;
    }

    /**
     * 检查是已经加密过的，就是查看文件头部的32个字节是否是{@link FileConstants#FILE_DOM}
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
            if (dom[i] != encodeFileDom[i]) throw new UnsupportEncode();
        }
    }


    private static void fileVerify(RandomAccessFile randomAccessFile) throws UnsupportEncode, IOException {
        byte[] dom = new byte[32];
        randomAccessFile.seek(0);
        randomAccessFile.readFully(dom);
        fileVerify(dom);
    }

    /**
     * 读取文件的如下字段：
     * 文件的格式(char)  32字节，
     * 程序的版本号(int) 4字节
     * 加密算法的版本号(int) 4字节
     * 随即填充字符(char) 16字节
     * 原始文件长度(long) 8字节
     *
     * @param randomAccessFile
     * @param outFileInfo
     * @throws IOException
     */
    public static void readFirst64Bytes(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        byte[] data = new byte[64];
        randomAccessFile.seek(0);
        randomAccessFile.readFully(data);
        outFileInfo.dom = new String(data, 0, 32);
        outFileInfo.softwareVersion = ByteUtil.bytesToInt(data, 32);
        outFileInfo.encodeVersion = ByteUtil.bytesToInt(data, 36);
        outFileInfo.originalFileLength = ByteUtil.bytesToLong(data, 56);
    }

    // 读取原始的文件名称
    public static void readOriginalFileName(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        randomAccessFile.seek(64);
        int length = FileConstants.readInt(randomAccessFile, -1);
        byte[] buff = new byte[length];
        randomAccessFile.readFully(buff);
        outFileInfo.originalFileName = new String(buff);
    }

    /**
     * 解析如下字段：
     * 原始文件修改时间(long) 8字节
     * 原始文件类型(char)     32字节
     * 移动的块大小(byte, 1024 * 2 * X)   4字节
     *
     * @param randomAccessFile
     * @param outFileInfo
     */
    private static void readExtraHeadInfo(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        byte[] buff = new byte[44];
        randomAccessFile.readFully(buff);
        outFileInfo.originalModifyTimeStamp = ByteUtil.bytesToLong(buff);
        outFileInfo.originalMimeType = new String(buff, 8, 32);
        outFileInfo.transferSize = ByteUtil.bytesToInt(buff, 40);
    }

    // 读取原始的头部和尾部的区域
    private static void readOriginalHeaderAndFooterRange(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        Preconditions.checkState(outFileInfo.transferSize > 0);
        FileInfo.Range footRange = new FileInfo.Range();
        footRange.count = outFileInfo.transferSize;
        footRange.offset = outFileInfo.originalFileLength - outFileInfo.transferSize;
        outFileInfo.originalFileFooterRange = footRange;

        FileInfo.Range headRange = new FileInfo.Range();
        headRange.count = outFileInfo.transferSize;
        headRange.offset = outFileInfo.originalFileLength;
        outFileInfo.originalFileHeaderRange = headRange;
    }

    // 读取thumbnail的信息
    private static void readThumbnailRange(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        int length = FileConstants.readInt(randomAccessFile, randomAccessFile.length() - 4);
        if (length <= 0) return;
        FileInfo.Range thumbnailRange = new FileInfo.Range();
        thumbnailRange.offset = randomAccessFile.length() - 4 - length;
        thumbnailRange.count = length;
        outFileInfo.thumbnailRange = thumbnailRange;
    }

    // 读取extra信息和加密时间戳
    private static void readExtraMessage(RandomAccessFile randomAccessFile, FileInfo outFileInfo) throws IOException {
        Preconditions.checkState(outFileInfo.originalFileLength > 0);
        randomAccessFile.seek(outFileInfo.originalFileLength + outFileInfo.transferSize);
        byte[] buff = new byte[FileConstants.EXTRA_MESSAGE_SIZE];
        randomAccessFile.readFully(buff);
        outFileInfo.extraTag = buff;
        byte[] et = new byte[8];
        randomAccessFile.readFully(et);
        outFileInfo.encodeTime = ByteUtil.bytesToLong(et);
    }
}
