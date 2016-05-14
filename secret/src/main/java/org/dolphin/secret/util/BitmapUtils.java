package org.dolphin.secret.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.ThumbnailUtils;
import android.util.Log;

import org.dolphin.lib.ValueReference;
import org.dolphin.lib.util.IOUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class BitmapUtils {
    public static final String TAG = "BitmapUtils";
    public static final BitmapUtils DEFAULT = new BitmapUtils();

    /**
     * 在缩放的时候，是否采用小值，{@code true}scale时选取宽高大的比例，否则选取宽高小的比例
     */
    private final boolean zoomImage = false;
    /**
     * 计算大小时是按照宽高计算还是采用消耗内存大小计算，{@code true}按照宽高大小计算缩放比例
     */
    private final boolean resizeBySize = false;

    /**
     * 是否尝试从MediaStore中得到缩略图,{@code true} 会尝试从系统缓存中得到，否则，不会从系统缓存中得到
     */
    private final boolean tryExtraFromMediaStore = false;

    private final float 

    private BitmapUtils(boolean zoomImage, boolean resizeBySize) {
        this.zoomImage = zoomImage;
        this.resizeBySize = resizeBySize;
    }


    public static void recycle(Bitmap bitmap) {
        if (null == bitmap || bitmap.isRecycled()) {
            return;
        }
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
            final ValueReference<Integer> sampleRef = new ValueReference<Integer>();
            final ValueReference<Float> scaleRef = new ValueReference<Float>();
            calculateSampleAndScale(options.outWidth, options.outHeight, expectWidth, expectHeight, sampleRef, scaleRef);
            if (null != sampleRef.getValue()) {
                options.inSampleSize = sampleRef.getValue();
            } else {
                options.inSampleSize = 1;
            }
            options.inJustDecodeBounds = false;
            options.inDither = false;
            Bitmap res = BitmapFactory.decodeFile(filePath, options);

            if (null != scaleRef.getValue()) {
                Matrix matrix = new Matrix();
                matrix.setScale(scales.getValue(), scales.getValue());
                Bitmap res1 = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
                recycle(res);
                res = res1;
            }

            return res;
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
    public static int calculateSampleBySize(int originalWidth, int originalHeight, int maxOutWidth, int maxOutHeight) {
        int inSample = 1;
        while (originalWidth > maxOutWidth || originalHeight > maxOutHeight) {
            inSample *= 2;
            originalWidth /= 2;
            originalHeight /= 2;
        }
        return inSample;
    }


    public static int calculateSampleByCount() {
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

    /**
     * 根据原始大小和输出的大小，计算采样率和scale系数
     *
     * @param originalWidth  original width
     * @param originalHeight original height
     * @param width          target width
     * @param height         target height
     * @param outInSample    in sample
     * @param outScale       out scale factor
     */
    public static void calculateSampleAndScale(int originalWidth, int originalHeight, int width, int height,
                                               ValueReference<Integer> outInSample, ValueReference<Float> outScale) {
        if (width == originalWidth && originalHeight == height) {
            outInSample.setValue(Integer.valueOf(1));
            outScale.setValue(null);
            return;
        }
        if (width >= originalWidth || height >= originalHeight) {
            outInSample.setValue(Integer.valueOf(1));
            outScale.setValue(BitmapUtils.calculateShrinkScale(originalWidth, originalHeight, width, height));
            return;
        }
        int ow = originalWidth;
        int oh = originalHeight;
        int inSample = 1;
        do {
            inSample *= 2;
            ow /= 2;
            oh /= 2;
        } while (ow >= width && oh >= height);
        inSample = inSample / 2 <= 1 ? 1 : inSample / 2;
        ow = originalWidth / inSample;
        oh = originalHeight / inSample;

        outInSample.setValue(Integer.valueOf(inSample));
        if (MathUtils.equals(ow, width) && MathUtils.equals(oh, height)) {
            outScale.setValue(null);
            return;
        }

        outScale.setValue(BitmapUtils.calculateShrinkScale(ow, oh, width, height));
    }


    /**
     * 计算采样频率，首先使用采样率，如果只修改采样率就能满足范围，则只是可以不scale，否则会使用采样率和scale配合
     *
     * @param originalWidth  original width
     * @param originalHeight original height
     * @param sizeRange      期望的范围
     * @param outInSample
     * @param outScale
     */
    public static void calculateSampleAndScale(int originalWidth, int originalHeight, BitmapSizeRange sizeRange,
                                               ValueReference<Integer> outInSample, ValueReference<Float> outScale) {
        if (sizeRange.validate(originalWidth, originalHeight)) {
            outInSample.setValue(Integer.valueOf(1));
            outScale.setValue(null);
            return;
        }

        if (originalWidth <= sizeRange.minWidth || originalHeight >= sizeRange.minHeight) {
            outInSample.setValue(Integer.valueOf(1));
            outScale.setValue(BitmapUtils.calculateShrinkScale(originalWidth, originalHeight, sizeRange.expectWidth, sizeRange.expectHeight));
            return;
        }
        float topScale = BitmapUtils.calculateShrinkScale(originalWidth, originalHeight, sizeRange.maxWidth, sizeRange.maxHeight);
        float scale = BitmapUtils.calculateShrinkScale(originalWidth, originalHeight, sizeRange.expectWidth, sizeRange.expectHeight);
        float lowScale = BitmapUtils.calculateShrinkScale(originalWidth, originalHeight, sizeRange.minWidth, sizeRange.minHeight);
        int sample = 1;
        final List<Integer> validateSamples = new ArrayList<Integer>();
        while (sample <= topScale) {
            if (sample >= lowScale && sample <= topScale) {
                validateSamples.add(Integer.valueOf(sample));
            }
            sample *= 2;
        }
        if (validateSamples.isEmpty()) {
            outInSample.setValue(null);
            outScale.setValue(Float.valueOf(scale));
        }

        outInSample.setValue(validateSamples.get(0));
        outScale.setValue(null);
    }

    public static Bitmap decode(byte[] source, Integer sample, Float scales, BitmapFactory.Options options) {
        if (null == options) {
            options = new BitmapFactory.Options();
        }
        options.inJustDecodeBounds = false;
        options.inDither = false;
        if (null != sample) {
            options.inSampleSize = sample;
        }
        Bitmap res = BitmapFactory.decodeByteArray(source, 0, source.length, options);

        if (null != scales && null != scales) {
            Matrix matrix = new Matrix();
            matrix.setScale(scales, scales);
            Bitmap res1 = Bitmap.createBitmap(res, 0, 0, res.getWidth(), res.getHeight(), matrix, true);
            recycle(res);
            res = res1;
        }
        return res;
    }

    public static Bitmap extractThumbnail(Bitmap source, int width, int height) {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }

    static class BitmapSizeRange {
        int expectWidth;
        int expectHeight;
        int minWidth;
        int minHeight;
        int maxWidth;
        int maxHeight;

        boolean validate(int width, int height) {
            if (width >= minWidth && width <= maxWidth
                    && height >= minHeight && height <= maxHeight) {
                return true;
            }
            return false;
        }
    }
}
