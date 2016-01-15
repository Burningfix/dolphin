package org.dolphin.secret.coder;

import org.dolphin.http.MimeType;
import org.dolphin.lib.ByteUtil;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.MimeTypeMap;
import org.dolphin.secret.FileConstants;
import org.dolphin.secret.FileInfo;

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

    public static boolean unEncodedFile(byte[] dom) {
        byte[] encodeFileDom = FileConstants.getFileDom();
        if (dom == null || encodeFileDom == null) return false;
        if (dom.length != encodeFileDom.length) return true;
        int length = dom.length;
        for (int i = 0; i < length; ++i) {
            if (dom[i] != encodeFileDom[i]) return false;
        }
        return true;
    }

    public void encode() throws UnsupportEncode, IOException {
        RandomAccessFile randomAccessFile = null;

        try {
            randomAccessFile = new RandomAccessFile(originalFile, "rw");
            byte[] dom = new byte[32];
            randomAccessFile.read(dom);
        } finally {
            IOUtil.safeClose(randomAccessFile);
        }
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
