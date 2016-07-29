package org.dolphin.secret.core;

import android.util.Log;

import org.apache.commons.io.FileUtils;
import org.dolphin.job.Operator;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.secret.browser.BrowserManager;

import java.io.File;
import java.io.IOException;
import java.io.RandomAccessFile;
import java.util.Random;

/**
 * Created by hanyanan on 2016/1/18.
 * <p/>
 * 还原已经加密混淆的文件
 */
public class DeobscureOperator implements Operator<ObscureFileInfo, File> {
    public static final String TAG = "DeobscureOperator";
    public static final DeobscureOperator DEFAULT = new DeobscureOperator(BrowserManager.getInstance().getRootDir());
    private final File rootDir;
    private final Random RANDOM = new Random();

    public DeobscureOperator(File rootDir) {
        this.rootDir = rootDir;
    }

    private int getRandomSuffix() {
        return Math.abs(RANDOM.nextInt()) / 1000000;
    }

    @Override
    public File operate(ObscureFileInfo obscureFile) throws Throwable {
        ObscureFileInfo fileInfo = obscureFile;
        RandomAccessFile randomAccessFile = null;
        File obscuredFile = new File(rootDir, obscureFile.obscuredFileName);
        if (fileInfo.encodeVersion == 1) { // 局部加密，大文件模式
            byte[] proguardHeadData = new byte[fileInfo.transferSize];
            byte[] proguardFootData = new byte[fileInfo.transferSize];
            try {
                randomAccessFile = new RandomAccessFile(obscuredFile, "rw");
                readObscureHeadAndFoot(randomAccessFile, fileInfo, proguardHeadData, proguardFootData);
                randomAccessFile.seek(0);
                randomAccessFile.write(FileConstants.decode(proguardHeadData));
                randomAccessFile.seek(fileInfo.originalFileFooterRange.offset);
                randomAccessFile.write(FileConstants.decode(proguardFootData));
                randomAccessFile.setLength(fileInfo.originalFileLength);
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
            } finally {
                IOUtil.safeClose(randomAccessFile);
            }
        }
        // rename
        File originalFile = new File(this.rootDir, fileInfo.originalFileName);
        while (originalFile.exists()) {
            Log.d(TAG, "Try rename to " + originalFile.getAbsolutePath() + " exists!");
            originalFile = new File(this.rootDir, fileInfo.originalFileName + "_" + getRandomSuffix());
        }
        try {
            FileUtils.moveFile(obscuredFile, originalFile);
            return originalFile;
        } catch (IOException ex) {
            ex.printStackTrace();
            return obscuredFile;
        }
    }

    private static void readObscureHeadAndFoot(RandomAccessFile randomAccessFile, ObscureFileInfo fileInfo,
                                          byte[] outProguardHeadData, byte[] outProguardFootData) throws IOException {
        ObscureFileInfo.Range footRange = fileInfo.originalFileFooterRange;
        randomAccessFile.seek(footRange.offset);
        randomAccessFile.readFully(outProguardFootData);
        randomAccessFile.readFully(outProguardHeadData);
    }
}
