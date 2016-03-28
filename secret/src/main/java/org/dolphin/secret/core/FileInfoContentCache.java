package org.dolphin.secret.core;

import android.graphics.Bitmap;

/**
 * Created by yananh on 2016/1/18.
 */
public class FileInfoContentCache {
    /**
     * 原始文件的头部信息
     */
    public byte[] headBodyContent;
    /**
     * 原始文件的尾部信息
     */
    public byte[] footBodyContent;
    /**
     * 原始文件的thumbnail
     */
    public Bitmap thumbnail;
}
