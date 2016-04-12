package org.dolphin.secret.util;

import android.graphics.Bitmap;
import android.provider.MediaStore;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.ValueReference;

/**
 * Created q    by yananh on 2016/4/7.
 */
public class ThumbnailHelper {
    /**
     * 严格的限制阈值
     */
    public static final float TOLERANCE = 0.5F;

    /**
     * 返回指定文件的thumbnail, 查询步骤为
     * 1. 尝试从数据库中得到已存在的thumbnail;
     * 2. 如果是图片则尝试从EXIF中得到，不是图片，则直接进入到第三步；
     * 3. 尝试decode原始文件，尝试从其中得到thumbnail
     * <p/>
     * 则尝试从现有的文件中得到thumbnail<br>
     * 输出的大小不能超过原始图片/电影的分辨率<br>
     *
     * @param path      原始文件路径
     * @param width     target width
     * @param height    target height
     * @param tolerance 允许的误差，取值范围为[0, 1], 越趋近于0，则表示输出必须完全等于制定的长宽，
     *                  1则表示可以是任意大小
     * @return
     */
    public static Bitmap getThumbnail(String path, int width, int height, float lowTolerance, float topTolerance) {
        BitmapSizeRange sizeRange = calculateBitmapSizeRange(width, height, lowTolerance, topTolerance);
        final ValueReference<Boolean> allowed = new ValueReference<Boolean>();
        final ValueReference<Integer> kind = new ValueReference<Integer>();
        final ValueReference<Float> scale = new ValueReference<Float>();
        allowMediaStoreThumbnail(sizeRange, allowed, kind, scale);
        if(allowed.getValue()) {
            Bitmap bitmap = getThumbnailFromMediaStore(path, kind.getValue(), scale.getValue());
            if (null != bitmap) {
                return bitmap;
            }
            bitmap.recycle();
        }





        return null;
    }

    public static Bitmap getThumbnailFromMediaStore(String filePath, int kind) {
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                f1z getContentResolver(), selectedImageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                (BitmapFactory.Options) null);

        return null;
    }

    public static Bitmap getThumbnailFromMediaStore(String filePath, int kind, float scale) {
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                f1z getContentResolver(), selectedImageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                (BitmapFactory.Options) null);

        return null;
    }


    /**
     * 是否支持从MediaStore中得到文件的thumbnail。
     *
     * @param range      期望得到thumbnail的
     * @param outAllowed 输出，是否支持从MediaStore中获取
     * @param outKind    如果支持从MediaStore获取，则返回指定kind（micro或者MINI）
     * @param outScale   如果支持从MediaStore中获取，返回指定的scale因子, 如果为null，则表示不需要进行scale
     */
    private static void allowMediaStoreThumbnail(BitmapSizeRange range, ValueReference<Boolean> outAllowed,
                                                 ValueReference<Integer> outKind, ValueReference<Float> outScale) {
        //  MINI_KIND: 512 x 384 thumbnail
        //  MICRO_KIND: 96 x 96 thumbnail
        // step 1. 判断是否支持，即MINI模式不能低于期望的最小值
        if (range.minWidth > 512 || range.minHeight > 384) {
            outAllowed.setValue(Boolean.FALSE);
            outScale.setValue(null);
            outKind.setValue(null);
            return;
        }

        // step 2. 判断MICRO Kind是否可行
        if (range.minWidth <= 96 || range.minHeight <= 96) {
            outAllowed.setValue(Boolean.TRUE);
            outKind.setValue(Integer.valueOf(MediaStore.Images.Thumbnails.MICRO_KIND));
            if (96 <= range.maxWidth && 96 <= range.maxHeight) {
                outScale.setValue(null);
            } else {
                outScale.setValue(Float.valueOf(calculateScale(96, 96, range.expectWidth, range.expectHeight)));
            }
            return;
        }

        // step 3. 判断MINI是否可行
        if (range.minWidth <= 512 || range.minHeight <= 384) {
            outAllowed.setValue(Boolean.TRUE);
            outKind.setValue(Integer.valueOf(MediaStore.Images.Thumbnails.MINI_KIND));
            if (512 <= range.maxWidth && 384 <= range.maxHeight) {
                outScale.setValue(null);
            } else {
                outScale.setValue(Float.valueOf(calculateScale(512, 384, range.expectWidth, range.expectHeight)));
            }
            return;
        }
        outAllowed.setValue(Boolean.FALSE);
        outScale.setValue(null);
        outKind.setValue(null);
    }


    /**
     * 计算允许的size范围
     *
     * @param expectWidth  expect width
     * @param expectHeight expect height
     * @param lowTolerance 允许的下限误差比例
     * @param topTolerance 允许的上限误差比例
     */
    private static BitmapSizeRange calculateBitmapSizeRange(int expectWidth, int expectHeight, float lowTolerance, float topTolerance) {
        lowTolerance = Math.max(0.0F, Math.min(1.0F, lowTolerance));
        topTolerance = Math.max(0.0F, Math.min(1.0F, topTolerance));
        BitmapSizeRange range = new BitmapSizeRange();
        range.expectWidth = expectWidth;
        range.expectHeight = expectHeight;
        range.minWidth = Math.round(expectWidth * (1 - lowTolerance));
        range.minHeight = Math.round(expectHeight * (1 - lowTolerance));
        range.maxWidth = Math.round(expectWidth * (1 + topTolerance));
        range.maxHeight = Math.round(expectHeight * (1 + topTolerance));
        return range;
    }

    /**
     * 计算采样, 查找最接近期望的采样率
     *
     * @param originalWidth
     * @param originalHeight
     * @param maxOutWidth
     * @param maxOutHeight
     * @return
     */
    public static int calculateInSample(int originalWidth, int originalHeight, int maxOutWidth, int maxOutHeight) {
        int inSample = 1;
        while (originalWidth > maxOutWidth || originalHeight > maxOutHeight) {
            inSample *= 2;
            originalWidth /= 2;
            originalHeight /= 2;
        }
        return inSample;
    }

    /**
     * 计算缩放比例，保证缩放后的长度和高度都不会超过期望的大小，图片必须保留宽高等比例缩放
     *
     * @param originalWidth  original width
     * @param originalHeight original height
     * @param expectWidth    期望的宽度
     * @param expectHeight   期望的长度
     * @return
     */
    public static float calculateScale(int originalWidth, int originalHeight, int expectWidth, int expectHeight) {
        float hScale = expectWidth / (float) originalWidth;
        float vScale = expectHeight / (float) originalHeight;
        return Math.min(hScale, vScale);
    }


    private static class BitmapSizeRange {
        private int expectWidth;
        private int expectHeight;
        private int minWidth;
        private int minHeight;
        private int maxWidth;
        private int maxHeight;
    }
}
