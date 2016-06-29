package org.dolphin.secret.core;

import org.apache.commons.io.FileUtils;
import org.dolphin.job.Operator;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.secret.SecretApplication;
import org.dolphin.secret.browser.BrowserManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;

/**
 * Created by hanyanan on 2016/1/18.
 * <p/>
 * 还原已经加密混淆的文件
 */
public class DeobscureOperator implements Operator<FileInfo, File> {
    public static final DeobscureOperator DEFAULT = new DeobscureOperator(BrowserManager.getInstance().getRootDir());
    private final File rootDir;

    public DeobscureOperator(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public File operate(FileInfo obscureFile) throws Throwable {
        FileInfo fileInfo = obscureFile;
        RandomAccessFile randomAccessFile = null;
        boolean recoverSuccessful = false;
        File obscuredFile = new File(rootDir, obscureFile.obscuredFileName);
        if (fileInfo.encodeVersion == 1) { // 局部加密，大文件模式
            byte[] proguardHeadData = new byte[fileInfo.transferSize];
            byte[] proguardFootData = new byte[fileInfo.transferSize];
            try {
                randomAccessFile = new RandomAccessFile(obscuredFile, "rw");
                decodeHeadAndFoot(randomAccessFile, fileInfo, proguardHeadData, proguardFootData);
                randomAccessFile.seek(0);
                randomAccessFile.write(FileConstants.decode(proguardHeadData));
                randomAccessFile.seek(fileInfo.originalFileFooterRange.offset);
                randomAccessFile.write(FileConstants.decode(proguardFootData));
                randomAccessFile.setLength(fileInfo.originalFileLength);
                recoverSuccessful = true;
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }
        } else { // 局部模式
            try {
                randomAccessFile = new RandomAccessFile(obscuredFile, "rw");
                long currLength = obscuredFile.length();
                randomAccessFile.seek(currLength - fileInfo.originalFileLength);
                byte[] body = new byte[(int) fileInfo.originalFileLength];
                randomAccessFile.readFully(body);
                randomAccessFile.seek(0);
                randomAccessFile.write(FileConstants.decode(body));
                randomAccessFile.setLength(fileInfo.originalFileLength);
                recoverSuccessful = true;
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }
        }

        if (!recoverSuccessful) {
            // TODO
            throw new IOException("Deobscure " + obscuredFile.getAbsolutePath() + " Failed!!!");
        }
        // rename
        File originalFile = new File(this.rootDir, fileInfo.originalFileName);
        try {
            FileUtils.moveFile(obscuredFile, originalFile);
            return originalFile;
        } catch (IOException ex) {
            ex.printStackTrace();
            return obscuredFile;
        }
    }

    private static void decodeHeadAndFoot(RandomAccessFile randomAccessFile, FileInfo fileInfo,
                                          byte[] outProguardHeadData, byte[] outProguardFootData) throws IOException {
        FileInfo.Range footRange = fileInfo.originalFileFooterRange;
        randomAccessFile.seek(footRange.offset);
        randomAccessFile.readFully(outProguardFootData);
        randomAccessFile.readFully(outProguardHeadData);
    }
}
