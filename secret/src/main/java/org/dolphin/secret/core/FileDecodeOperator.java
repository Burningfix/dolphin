package org.dolphin.secret.core;

import org.dolphin.job.Operator;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/18.
 */
public class FileDecodeOperator implements Operator<File, FileInfo> {
    @Override
    public FileInfo operate(File input) throws Throwable {
        FileInfo fileInfo = null;
        RandomAccessFile randomAccessFile = null;
        try {
            FileInfoReaderOperator fileInfoReaderOperator = new FileInfoReaderOperator();
            fileInfo = fileInfoReaderOperator.operate(input);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        }
        byte[] proguardHeadData = new byte[fileInfo.transferSize];
        byte[] proguardFootData = new byte[fileInfo.transferSize];

        try {
            randomAccessFile = new RandomAccessFile(input, "rw");
            decodeHeadAndFoot(randomAccessFile, fileInfo, proguardHeadData, proguardFootData);
            randomAccessFile.seek(0);
//            randomAccessFile.write(FileConstants.decode(proguardHeadData));
            randomAccessFile.write(proguardHeadData);
            randomAccessFile.seek(fileInfo.originalFileFooterRange.offset);
//            randomAccessFile.write(FileConstants.decode(proguardFootData));
            randomAccessFile.write(proguardFootData);
            randomAccessFile.setLength(fileInfo.originalFileLength);
        } finally {
            IOUtil.safeClose(randomAccessFile);
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
