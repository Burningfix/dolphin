package org.dolphin.secret.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.graphics.Matrix;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.dolphin.lib.ValueReference;
import org.dolphin.lib.util.IOUtil;

import java.io.FileDescriptor;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class BitmapUtils {
    public static final String TAG = "BitmapUtils";

    public static boolean checkOptions(BitmapFactory.Options options) {
        if (null == options || options.mCancel
                || options.outWidth == -1 || options.outHeight == -1) {
            return false;
        }
        return true;
    }

    public static boolean checkBitmap(Bitmap bitmap) {
        if (null == bitmap || bitmap.isRecycled() || bitmap.getWidth() <= 0 || bitmap.getHeight() <= 0) {
            return false;
        }
        return true;
    }

    public static void recycle(Bitmap bitmap) {
        if (null == bitmap || bitmap.isRecycled()) {
            return;
        }
        bitmap.recycle();
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

    public static float calculateScale(int originalWidth, int originalHeight,
                                       int expectWidth, int expectHeight, boolean zoomScale) {
        return zoomScale ? calculateZoomScale(originalWidth, originalHeight, expectWidth, expectHeight)
                : calculateShrinkScale(originalWidth, originalHeight, expectWidth, expectHeight);
    }

    /**
     * 根据高宽计算采样,  输出的不得低于制定的大小
     *
     * @param originalWidth
     * @param originalHeight
     * @param minOutWidth    输出的宽度的下限
     * @param minOutHeight   输出的高度的下限
     * @return
     */
    public static int calculateInSampleBySize(int originalWidth, int originalHeight, int minOutWidth, int minOutHeight) {
        int ow = originalWidth;
        int oh = originalHeight;
        int inSample = 1;
        do {
            inSample *= 2;
            ow /= 2;
            oh /= 2;
        } while (ow >= minOutWidth && oh >= minOutHeight);
        inSample = inSample / 2 <= 1 ? 1 : inSample / 2;
        return inSample;
    }

    public static int calculateInSampleByCount(int originalWidth, int originalHeight, int maxOutWidth, int maxOutHeight) {
        return 1;
    }

    public static Bitmap extractBitmap(Bitmap source, int width, int height) {
        return ThumbnailUtils.extractThumbnail(source, width, height);
    }

    public static Bitmap extractBitmap(Bitmap source, float scale) {
        if (null == source) {
            return null;
        }
        Bitmap dest = Bitmap.createScaledBitmap(source, (int) (source.getWidth() * scale),
                (int) (source.getHeight() * scale), false);
        recycle(source);
        return dest;
    }

    public static Bitmap decodeBitmap(byte[] data, int expectWidth, int expectHeight,
                                      BitmapFactory.Options options) {
        return decodeBitmap(data, expectWidth, expectHeight, options, true, true);
    }

    public static Bitmap decodeBitmap(byte[] data, int expectWidth, int expectHeight,
                                      BitmapFactory.Options options,
                                      boolean supportInSample, boolean zoomScale) {
        if (null == data || data.length <= 0) {
            return null;
        }
        Bitmap res = null;
        if (null == options) {
            options = new BitmapFactory.Options();
        }
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (!checkOptions(options)) {
            return null;
        }
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        // case 1. 长宽都小于目标值，返回当前bitmap
        if (originalWidth <= expectWidth && originalHeight <= expectHeight) {
            return null;
        }
        // case 2. 宽高至少有一个小于目标值，根据scale采取措施
        if (originalWidth <= expectWidth || originalHeight <= expectHeight) {
            res = BitmapFactory.decodeByteArray(data, 0, data.length, options);
            if (!checkBitmap(res)) {
                return null;
            }
            return extractBitmap(res, calculateScale(originalWidth, originalHeight,
                    expectWidth, expectHeight, zoomScale));
        }
        // case 3. 宽高都大于目标值
        options.inSampleSize = supportInSample ?
                calculateInSampleBySize(originalWidth, originalHeight, expectWidth, expectHeight) : 1;
        res = BitmapFactory.decodeByteArray(data, 0, data.length, options);
        if (!checkBitmap(res)) {
            recycle(res);
            return null;
        }

        return extractBitmap(res,
                calculateScale(res.getWidth(), res.getHeight(), expectWidth, expectHeight, zoomScale));
    }

    public static Bitmap decodeBitmap(String filePath, int expectWidth, int expectHeight,
                                      BitmapFactory.Options options,
                                      boolean supportInSample, boolean zoomScale) {
        if (TextUtils.isEmpty(filePath)) {
            return null;
        }
        Bitmap res = null;
        if (null == options) {
            options = new BitmapFactory.Options();
        }
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        BitmapFactory.decodeFile(filePath, options);
        if (!checkOptions(options)) {
            return null;
        }
        int originalWidth = options.outWidth;
        int originalHeight = options.outHeight;
        options.inJustDecodeBounds = false;
        // case 1. 长宽都小于目标值，返回null
        if (originalWidth <= expectWidth && originalHeight <= expectHeight) {
            return null;
        }
        // case 2. 宽高至少有一个小于目标值，根据scale采取措施
        if (originalWidth <= expectWidth || originalHeight <= expectHeight) {
            res = BitmapFactory.decodeFile(filePath, options);
            if (!checkBitmap(res)) {
                return null;
            }
            return extractBitmap(res, calculateScale(originalWidth, originalHeight,
                    expectWidth, expectHeight, zoomScale));
        }
        // case 3. 宽高都大于目标值, 先计算采样率，在使用
        options.inSampleSize = supportInSample ?
                calculateInSampleBySize(originalWidth, originalHeight, expectWidth, expectHeight) : 1;
        res = BitmapFactory.decodeFile(filePath, options);
        if (!checkBitmap(res)) {
            recycle(res);
            return null;
        }

        return extractBitmap(res,
                calculateScale(res.getWidth(), res.getHeight(), expectWidth, expectHeight, zoomScale));
    }

    public static Bitmap createBitmap(Bitmap source, int expectWidth, int expectHeight,
                                      BitmapFactory.Options options,
                                      boolean supportInSample, boolean zoomScale) {

        return null;
    }

    public static Bitmap extractFromMediaMetadataRetriever(String filePath) {
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

    private abstract static class BaseThumbnailUtils {
        private static final Map<Object, Long> FILE_ID_MAP = new ConcurrentHashMap<>();
        public static final float DIFFERENCE_FACTOR = 0.5F;

        /**
         * 在缩放的时候，是否采用小值，{@code true}scale时选取宽高大的比例，否则选取宽高小的比例
         */
        private boolean zoomImage = false;

        /**
         * 解析的thumbnail的上限的因子, 上限为<b>[expectValue, expectValue + expectValue * upperLimitFactor]</b>
         */
        private final float upperLimitFactor;

        /**
         * 解析thumbnail的下限因子，下限为<b>[expectValue-expectValue*lowerLimitFactor, expectValue]</b>
         */
        private final float lowerLimitFactor;

        private BaseThumbnailUtils(boolean zoomImage) {
            this.zoomImage = zoomImage;
            this.upperLimitFactor = 1;
            this.lowerLimitFactor = 0;
        }

        protected abstract Context getContext();

        protected boolean isZoomImage() {
            return this.zoomImage;
        }

        protected Uri getExternalUri() {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        protected Uri getThumbnailUri() {
            return MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        }

        public Bitmap extractThumbnailFromMediaStore(String path, SizeRange range) {
            // MINI_KIND: 512 x 384
            // MICRO_KIND: 96 x 96
            if (!range.inRange(512, 384)) {
                // out of range
                return null;
            }

            int kind = MediaStore.Images.Thumbnails.MICRO_KIND;
            if (range.inRange(96, 96)) {
                kind = MediaStore.Images.Thumbnails.MICRO_KIND;
            } else {
                kind = MediaStore.Images.Thumbnails.MINI_KIND;
            }

            Bitmap res = extraThumbnailFromMediaStore(path, kind);
            if (checkBitmap(res)) {
                Bitmap res1 = extractBitmap(res, range.expectWidth, range.expectHeight);
                recycle(res);
                return res1;
            }
            recycle(res);
            return null;
//            ContentResolver resolver = getContext().getContentResolver();
//            String[] projection = new String[]{"_data", "_id", "width", "height"};
//            String whereClause = "_data = '" + path + "'";
//            Cursor cursor = resolver.query(getExternalUri(), projection, whereClause, null, null);
//            Integer id = null;
//            int width = 0, height = 0;
//            if (null != cursor && cursor.moveToFirst()) {
//                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
//                width = cursor.getInt(cursor.getColumnIndex("width"));
//                height = cursor.getInt(cursor.getColumnIndex("height"));
//            }
//            if (null != cursor) {
//                cursor.close();
//            }
//            if (null == id || (width < expectWidth && height < expectHeight)) {
//                return null;
//            }
//            projection = new String[]{"_data"};
//            whereClause = "_id = '" + id + "'";
//            cursor = resolver.query(getExternalUri(), projection, whereClause, null, null);
//            try {
//                if (null != cursor && cursor.moveToFirst()) {
//                    return cursor.getBlob(cursor.getColumnIndex("_data"));
//                }
//            } finally {
//                if (null != cursor) {
//                    cursor.close();
//                }
//            }
////            Bitmap bitmap = MediaStore.Images.Thumbnails.getThumbnail(resolver, id, kind, options);
//            return null;
        }

        public abstract Bitmap extractThumbnailFromMediaStore(String path, int kine);

        public abstract byte[] extractThumbnailFromFileTag(String path);

        public abstract Bitmap decodeFile(String filePath, int expectWidth, int expectHeight,
                                          BitmapFactory.Options options);


        public Bitmap extraThumbnail(String filePath, int expectWidth, int expectHeight,
                                     BitmapFactory.Options options) {
            SizeRange range = computeSizeRange(expectWidth, expectHeight, upperLimitFactor, lowerLimitFactor);
            Bitmap res = null;
            if (null == options) {
                options = new BitmapFactory.Options();
            }
            options.inSampleSize = 1;
            // step 1. 从MediaStore中获取
            {
                byte[] data = extractThumbnailFromMediaStore(filePath, range);
                res = decodeBitmap(data, expectWidth, expectHeight, options, true, isZoomImage());
                if (checkBitmap(res)) {
                    return res;
                }
                recycle(res);
            }

            // step 2. 从文件tag中读取thumbnail，例如jpeg支持的exif
            {
                byte[] data = extractThumbnailFromFileTag(filePath);
                res = decodeBitmap(data, expectWidth, expectHeight, options, true, isZoomImage());
                if (checkBitmap(res)) {
                    return res;
                }
                recycle(res);
            }

            // step 3. 直接decode文件，尝试从文件中读取
            res = decodeFile(filePath, expectWidth, expectHeight, options);
            if (checkBitmap(res)) {
                return res;
            }
            recycle(res);
            return null;
        }

        protected static Bitmap processBitmap(Bitmap source, SizeRange range) {

        }

        protected static int computeInSample(int originalWidth, int originalHeight, SizeRange range) {
            return BitmapUtils.calculateInSampleBySize(originalWidth, originalHeight, range.minWidth, range.minHeight);
        }

        protected static SizeRange computeSizeRange(int width, int height, float upperLimitFactor, float lowerLimitFactor) {
            SizeRange range = new SizeRange();
            range.expectWidth = width;
            range.expectHeight = height;
            range.maxWidth = (int) (width + width * upperLimitFactor);
            range.maxHeight = (int) (height + height * upperLimitFactor);
            range.minWidth = (int) (width - width * lowerLimitFactor);
            range.minHeight = (int) (height - height * lowerLimitFactor);
            return range;
        }

        protected static class SizeRange {
            int expectWidth;
            int expectHeight;
            int minWidth;
            int minHeight;
            int maxWidth;
            int maxHeight;

            protected boolean inRange(int width, int height) {
                if (width >= minWidth && width <= maxWidth
                        && height >= minHeight && height <= maxHeight) {
                    return true;
                }
                return false;
            }
        }
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
    public static Bitmap decodeFile(String filePath, int expectWidth, int expectHeight,
                                    BitmapFactory.Options options, boolean zoomScale,
                                    boolean enableCalculateSample, boolean calculateSampleBySize) {
        if (null == options) {
            options = new BitmapFactory.Options();
        }
        options.inSampleSize = 1;
        options.inJustDecodeBounds = true;
        FileInputStream stream = null;
        Bitmap res = null;
        try {
            stream = new FileInputStream(filePath);
            FileDescriptor fd = stream.getFD();
            BitmapFactory.decodeFileDescriptor(fd, null, options);
            if (BitmapUtils.checkOptions(options)) {
                return null;
            }
            final int originalWidth = options.outWidth;
            final int originalHeight = options.outHeight;
            //step1. 原始图片太小，直接返回原始图片
            if (originalWidth <= expectWidth || originalHeight <= expectHeight) {
                res = BitmapFactory.decodeFile(filePath, options);
                return res;
            }


            if (enableCalculateSample) {
                final ValueReference<Integer> sampleRef = new ValueReference<Integer>();
                int sample = 1;
                if (calculateSampleBySize) {
                    sample = calculateSampleBySize(originalWidth, originalHeight, expectWidth, expectHeight);
                } else {
                    sample = calculateSampleByCount(originalWidth, originalHeight, expectWidth, expectHeight);
                }
                res = Bitmap.createBitmap()
            }


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
                                               boolean zoomImage, Boolean calculateSampleBySize,
                                               ValueReference<Integer> outInSample, ValueReference<Float> outScale) {
        if (width == originalWidth && originalHeight == height) {
            outInSample.setValue(Integer.valueOf(1));
            outScale.setValue(null);
            return;
        }
        if (width >= originalWidth || height >= originalHeight) {
            outInSample.setValue(Integer.valueOf(1));
            if (zoomImage) {
                outScale.setValue(calculateScale(originalWidth, originalHeight, width, height, zoomImage));
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
}
