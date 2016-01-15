package org.dolphin.secret;

import android.graphics.Bitmap;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class FileInfo {
    public String dom;
    public int softwareVersion;
    public int encodeVersion;
    public long originalFileLength;
    public long originalModifyTimeStamp;
    public String originalFileName;
    public String originalMimeType;
    public byte[] originalFileHeader;
    public byte[] originalFileFooter;
    public Bitmap thumbnail;
    public long encodeTime;
    public byte[] extraTag;
}
