package org.dolphin.secret.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ThumbnailUtils;
import android.util.Log;

import org.dolphin.lib.IOUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class BitmapUtils {
    public static final String TAG = "BitmapUtils";

    public static void recycle(Bitmap bitmap) {
        if (null == bitmap || bitmap.isRecycled()) return;
        bitmap.recycle();
    }

    public static boolean checkOptions(BitmapFactory.Options options) {
        if (null == options || options.mCancel
                || options.outWidth == -1 || options.outHeight == -1) {
            return false;
        }
        return true;
    }

    /**
     * 按照期望的大小得到当前图片的bitmap
     *
     * @param filePath
     * @param expectWidth
     * @param expectHeight
     * @param options
     * @return
     */
    public static Bitmap decodeImageFile(String filePath, int expectWidth, int expectHeight, BitmapFactory.Options options) {
        if (null == options) {
            options = new BitmapFactory.Options();
        }
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        FileInputStream stream = null;
        try {
            stream = new FileInputStream(filePath);
            FileDescriptor fd = stream.getFD();
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (BitmapUtils.checkOptions(options)) {
                return null;
            }
            options.inSampleSize = calculateInSampleBySize(options.outWidth, options.outHeight,
                    expectWidth, expectHeight);
            options.inJustDecodeBounds = false;
            options.inDither = false;
            return BitmapFactory.decodeFileDescriptor(fd, null, options);
        } catch (IOException ex) {
            Log.e(TAG, "", ex);
        } catch (OutOfMemoryError oom) {
            Log.e(TAG, "Unable to decode file " + filePath + ". OutOfMemoryError.", oom);
        } finally {
            IOUtil.closeQuietly(stream);
        }
        return null;
    }


    /**
     * 根据高宽计算采样, 查找最接近期望的采样率
     *
     * @param originalWidth
     * @param originalHeight
     * @param maxOutWidth
     * @param maxOutHeight
     * @return
     */
    public static int calculateInSampleBySize(int originalWidth, int originalHeight, int maxOutWidth, int maxOutHeight) {
        int inSample = 1;
        while (originalWidth > maxOutWidth || originalHeight > maxOutHeight) {
            inSample *= 2;
            originalWidth /= 2;
            originalHeight /= 2;
        }
        return inSample;
    }


    public static int calculateInSampleByCount() {
        return 1;
    }

    /**
     * 计算缩放比例，保证缩放后的长度和高度都不会超过期望的大小，图片必须保留宽高等比例缩放
     *
     * @param originalWidth  original width
     * @param originalHeight original height
     * @param expectWidth    期望的宽度
     * @param expectHeight   期望的长度
     */
    public static float calculateShrinkScale(int originalWidth, int originalHeight, int expectWidth, int expectHeight) {
        float hScale = expectWidth / (float) originalWidth;
        float vScale = expectHeight / (float) originalHeight;
        return Math.min(hScale, vScale);
    }

    /**
     * 计算缩放比例，采用缩放系数比较大的那个
     *
     * @param originalWidth  original width
     * @param originalHeight original height
     * @param expectWidth    期望的宽度
     * @param expectHeight   期望的长度
     */
    public static float calculateZoomScale(int originalWidth, int originalHeight, int expectWidth, int expectHeight) {
        float hScale = expectWidth / (float) originalWidth;
        float vScale = expectHeight / (float) originalHeight;
        return Math.max(hScale, vScale);
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }
}
