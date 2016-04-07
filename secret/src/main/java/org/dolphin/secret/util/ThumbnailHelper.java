package org.dolphin.secret.util;

import android.graphics.Bitmap;

/**
 * Created by yananh on 2016/4/7.
 */
public class ThumbnailHelper {


    /**
     * 返回指定文件的thumbnail, 查询步骤为
     * 1. 尝试从数据库中得到已存在的thumbnail;
     * 2. 如果是图片则尝试从EXIF中得到，不是图片，则直接进入到第三步；
     * 3. 尝试decode原始文件，尝试从其中得到thumbnail
     * <p/>
     * 则尝试从现有的文件中得到tumbnail
     *
     * @param path     原始文件路径
     * @param width    target width
     * @param height   target height
     * @param restrict 是否需要严格限制输出的bitmap的width和height, 如果严格限制，则返回指定的大小，
     *                 否则会尝试width和height为最大长款
     * @return
     */
    public static Bitmap getThumbnail(String path, int width, int height/*, boolean restrict*/) {


        return null;
    }


    /**
     * 计算采样
     *
     * @param originalWidth
     * @param originalHeight
     * @param exceptionWidth
     * @param exceptionHeight
     * @return
     */
    public static int calculateSampleSize(int originalWidth, int originalHeight, int exceptionWidth, int exceptionHeight) {
        int inSample = 1;
        while (originalWidth > exceptionWidth || originalHeight > exceptionHeight) {
            inSample *= 2;
            originalWidth /= 2;
            originalHeight /= 2;
        }
        return inSample;
    }


}
