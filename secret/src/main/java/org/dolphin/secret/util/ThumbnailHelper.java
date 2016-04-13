package org.dolphin.secret.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.provider.MediaStore;
import android.util.Log;

import org.dolphin.http.MimeType;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.ValueReference;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;

/**
 * Created q    by yananh on 2016/4/7.
 */
public class ThumbnailHelper {
    public static final String TAG = "ThumbnailHelper";

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
     * @param path         原始文件路径
     * @param width        target width
     * @param height       target height
     * @param lowTolerance 允许的误差，取值范围为[0, 1], 越趋近于0，则表示输出必须完全等于制定的长宽，
     *                     1则表示可以是任意大小
     * @param topTolerance 允许的误差，取值范围为[0, 1], 越趋近于0，则表示输出必须完全等于制定的长宽，
     *                     1则表示可以是任意大小
     * @return
     */
    public static Bitmap getThumbnail(String path, int width, int height, float lowTolerance, float topTolerance) {
        final BitmapSizeRange sizeRange = calculateBitmapSizeRange(width, height, lowTolerance, topTolerance);
        /*
        * step1. 尝试从MediaStore中得到缓存的thumbnail
        * */
        Bitmap bitmap = getThumbnailFromMediaStore(path, sizeRange);
        if (null != bitmap) return bitmap;

        /*
        * step2. 如果是Jpeg图片，则尝试从EXIF中得到thumbnail
        * */
        MimeType mimeType = MimeType.createFromFileName(path);
        if (mimeType.getMimeType().equals("image/jpeg")) { // MediaFile.getFileType(filePath);
            bitmap = createThumbnailFromEXIF(path, sizeRange);
            if (null != bitmap) {
                return bitmap;
            }
        }

        /*
        * step3. 解析文件，得到thumbnail
        * */
        if (mimeType.getMimeType().startsWith("image")) {
            return decodeImageFile(path, width, height, null);
        } else if (mimeType.getMimeType().startsWith("video")) {
            return createVideoThumbnail(path, width, height);
        } else if (mimeType.getMimeType().startsWith("audio")) {
            // TODO, not support now
        }
        return null;
    }


    public static Bitmap getThumbnailFromMediaStore(String filePath, int kind, Float scale) {
        Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(
                f1z getContentResolver(), selectedImageUri,
                MediaStore.Images.Thumbnails.MINI_KIND,
                (BitmapFactory.Options) null);

        return null;
    }

    /**
     * 尝试从MediaStore中尝试得到指定的thumbnail，如果没有，则返回null
     *
     * @param filePath  原始文件的绝度路径
     * @param sizeRange 期望输出的长宽范围
     * @return 如果符合条件，则返回指定bitmap，否则返回null
     */
    public static Bitmap getThumbnailFromMediaStore(String filePath, BitmapSizeRange sizeRange) {
        final ValueReference<Boolean> allowed = new ValueReference<Boolean>();
        final ValueReference<Integer> kind = new ValueReference<Integer>();
        final ValueReference<Float> scale = new ValueReference<Float>();
        allowMediaStoreThumbnail(sizeRange, allowed, kind, scale);
        if (allowed.getValue()) {
            Bitmap bitmap = getThumbnailFromMediaStore(filePath, kind.getValue(), scale.getValue());
            if (null != bitmap) {
                return bitmap;
            }
            BitmapUtils.recycle(bitmap);
        }
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
                outScale.setValue(Float.valueOf(calculateZoomScale(96, 96, range.expectWidth, range.expectHeight)));
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
                outScale.setValue(Float.valueOf(calculateZoomScale(512, 384, range.expectWidth, range.expectHeight)));
            }
            return;
        }
        outAllowed.setValue(Boolean.FALSE);
        outScale.setValue(null);
        outKind.setValue(null);
    }

    /**
     * Creates a bitmap by either downsampling from the thumbnail in EXIF or the full image.
     * The functions returns a SizedThumbnailBitmap,
     * which contains a downsampled bitmap and the thumbnail data in EXIF if exists.
     */
    public static Bitmap createThumbnailFromEXIF(String filePath, BitmapSizeRange sizeRange) {
        ExifInterface exif = null;
        byte[] thumbData = null;
        try {
            exif = new ExifInterface(filePath);
            thumbData = exif.getThumbnail();
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }

        BitmapFactory.Options exifOptions = new BitmapFactory.Options();

        // Compute exifThumbWidth.
        if (thumbData != null) {
            exifOptions.inJustDecodeBounds = true;
            BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
            if (exifOptions.outWidth < sizeRange.minWidth || exifOptions.outHeight < sizeRange.minHeight) {
                return null;
            }

            if (exifOptions.outWidth <= sizeRange.maxWidth && exifOptions.outHeight <= sizeRange.maxHeight) {
                exifOptions.inJustDecodeBounds = false;
                exifOptions.inPreferredConfig = Bitmap.Config.ARGB_8888;
                return BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, exifOptions);
            }
        }
        return null;
    }

    /**
     * 尝试从Jpeg文件中得到EXIF信息
     *
     * @param filePath 原始的Jpeg文件
     * @param options  解析的配置
     * @return
     */
    public static Bitmap createThumbnailFromEXIF(String filePath, BitmapFactory.Options options) {
        if (null == options) {
            options = new BitmapFactory.Options();
        }

        ExifInterface exif = null;
        byte[] thumbData = null;
        try {
            exif = new ExifInterface(filePath);
            thumbData = exif.getThumbnail();
        } catch (IOException ex) {
            Log.w(TAG, ex);
        }

        // Compute exifThumbWidth.
        if (thumbData != null) {
            options.inJustDecodeBounds = false;
            Bitmap res = BitmapFactory.decodeByteArray(thumbData, 0, thumbData.length, options);
            if (BitmapUtils.checkOptions(options)) {
                BitmapUtils.recycle(res);
                return null;
            }
            return res;
        }
        return null;
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

    public static Bitmap createVideoThumbnail(String filePath) {
        Bitmap bitmap = null;
        MediaMetadataRetriever retriever = new MediaMetadataRetriever();
        try {
            retriever.setDataSource(filePath);
            bitmap = retriever.getFrameAtTime(-1);
            return bitmap;
        } catch (IllegalArgumentException ex) {
            // Assume this is a corrupt video file
        } catch (RuntimeException ex) {
            // Assume this is a corrupt video file.
        } finally {
            try {
                retriever.release();
            } catch (RuntimeException ex) {
                // Ignore failures while cleaning up.
            }
        }

        return null;
    }

    public static Bitmap createVideoThumbnail(String filePath, int width, int height) {
        Bitmap bitmap = createVideoThumbnail(String filePath);
        if (null == bitmap) {
            return null;
        }

        return ThumbnailUtils.extractThumbnail(bitmap, width, height);
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

    private static class BitmapSizeRange {
        private int expectWidth;
        private int expectHeight;
        private int minWidth;
        private int minHeight;
        private int maxWidth;
        private int maxHeight;
    }
}
