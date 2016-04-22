package org.dolphin.secret.core;

import org.dolphin.job.Operator;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/18.
 *
 * 还原已经加密混淆的文件
 */
public class DeobscureOperator implements Operator<File, FileInfo> {
    @Override
    public FileInfo operate(File input) throws Throwable {
        FileInfo fileInfo = null;
        RandomAccessFile randomAccessFile = null;
        try {
            ObscureFileInfoReaderOperator obscureFileInfoReaderOperator = ObscureFileInfoReaderOperator.DEFAULT;
            fileInfo = obscureFileInfoReaderOperator.operate(input);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
        boolean success = false;
        if (fileInfo.encodeVersion == 1) { // 局部加密，大文件模式
            byte[] proguardHeadData = new byte[fileInfo.transferSize];
            byte[] proguardFootData = new byte[fileInfo.transferSize];
            try {
                randomAccessFile = new RandomAccessFile(input, "rw");
                decodeHeadAndFoot(randomAccessFile, fileInfo, proguardHeadData, proguardFootData);
                randomAccessFile.seek(0);
                randomAccessFile.write(FileConstants.decode(proguardHeadData));
                randomAccessFile.seek(fileInfo.originalFileFooterRange.offset);
                randomAccessFile.write(FileConstants.decode(proguardFootData));
                randomAccessFile.setLength(fileInfo.originalFileLength);
                success = true;
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }
        } else { // 局部模式
            try {
                randomAccessFile = new RandomAccessFile(input, "rw");
                long currLength = input.length();
                randomAccessFile.seek(currLength - fileInfo.originalFileLength);
                byte[] body = new byte[(int) fileInfo.originalFileLength];
                randomAccessFile.readFully(body);
                randomAccessFile.seek(0);
                randomAccessFile.write(FileConstants.decode(body));
                randomAccessFile.setLength(fileInfo.originalFileLength);
                success = true;
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }

            if (success) {
                // TODO
            }
        }
        return fileInfo;
    }

    private static void decodeHeadAndFoot(RandomAccessFile randomAccessFile, FileInfo fileInfo,
                                          byte[] outProguardHeadData, byte[] outProguardFootData) throws IOException {
        FileInfo.Range footRange = fileInfo.originalFileFooterRange;
        randomAccessFile.seek(footRange.offset);
        randomAccessFile.readFully(outProguardFootData);
        randomAccessFile.readFully(outProguardHeadData);
    }
}
