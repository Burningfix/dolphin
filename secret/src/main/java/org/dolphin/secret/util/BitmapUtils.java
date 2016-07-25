package org.dolphin.secret.util;

import android.content.ContentResolver;
import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.ExifInterface;
import android.media.MediaMetadataRetriever;
import android.media.ThumbnailUtils;
import android.net.Uri;
import android.provider.MediaStore;
import android.text.TextUtils;
import android.util.Log;

import org.dolphin.http.MimeType;
import org.dolphin.secret.SecretApplication;

import java.io.File;
import java.io.IOException;
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

    public static boolean checkByteArray(byte[] data) {
        if (null == data || data.length <= 0) {
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

    public static int calculateInSampleByCount(int originalWidth, int originalHeight, int minOutWidth, int minOutHeight) {
        int inSample = 1;
        int original = originalWidth * originalHeight;
        int dest = minOutWidth * minOutHeight;
        do {
            inSample *= 2;
            original /= 2;
        } while (original >= dest);
        inSample = inSample / 2 <= 1 ? 1 : inSample / 2;
        return inSample;
    }

    public static Bitmap extractBitmap(Bitmap source, int width, int height) {
        if (!checkBitmap(source)) {
            return null;
        }
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

        protected BaseThumbnailUtils(boolean zoomImage, float upperLimitFactor, float lowerLimitFactor) {
            this.zoomImage = zoomImage;
            this.upperLimitFactor = upperLimitFactor;
            this.lowerLimitFactor = lowerLimitFactor;
        }

        protected Context getContext() {
            return SecretApplication.getInstance();
        }

        protected boolean isZoomImage() {
            return this.zoomImage;
        }

        protected Uri getExternalUri() {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        protected Uri getThumbnailUri() {
            return MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        }

        protected Bitmap extractThumbnailFromMediaStore(final String path, final SizeRange range) {
            // MINI_KIND: 512 x 384
            // MICRO_KIND: 96 x 96
            // 数据库中只有MICRO_KIND模式
            if (!range.withInInternal(96, 96)) {
                return null;
            }

            ContentResolver resolver = getContext().getContentResolver();
            String[] projection = new String[]{"_id"};
            String whereClause = "_data = '" + path + "'";
            Cursor cursor = resolver.query(getExternalUri(), projection, whereClause, null, null);
            Integer id = null;
            if (null != cursor && cursor.moveToFirst()) {
                id = cursor.getInt(cursor.getColumnIndex(MediaStore.Images.Media._ID));
            }
            if (null != cursor) {
                cursor.close();
            }
            if (null == id) {
                return null;
            }
            projection = new String[]{"_data"};
            whereClause = "_id = '" + id + "'";
            cursor = resolver.query(getThumbnailUri(), projection, whereClause, null, null);
            try {
                if (null != cursor && cursor.moveToFirst()) {
                    byte[] data = cursor.getBlob(cursor.getColumnIndex("_data"));
                    if (null == data || data.length <= 0) {
                        return null;
                    }
                    return BitmapFactory.decodeByteArray(data, 0, data.length);
                }
            } finally {
                if (null != cursor) {
                    cursor.close();
                }
            }
            return null;
        }

        protected abstract Bitmap extractThumbnailFromFileTag(String path, SizeRange range);

        protected abstract Bitmap decodeFile(String filePath, SizeRange range,
                                             BitmapFactory.Options options);

        /**
         * 尝试得到指定文件的thumbnail，尝试的渠道有三种：
         * 1. MediaStore的database， byte stream from database (MICRO_KIND)(96 x 96)
         * 2. 从file的tag中获取， 比如jpeg的exif中获取
         * 3. 解析整个文件，得到指定的thumbnail
         *
         * @param filePath
         * @param expectWidth
         * @param expectHeight
         * @param options
         * @return
         */
        public Bitmap extractThumbnail(String filePath, int expectWidth, int expectHeight,
                                       BitmapFactory.Options options) {
            SizeRange range = computeSizeRange(expectWidth, expectHeight, upperLimitFactor, lowerLimitFactor);
            Bitmap res = null;
            if (null == options) {
                options = new BitmapFactory.Options();
            }
            options.inSampleSize = 1;
            // step 1. 从MediaStore中获取
            {
                res = extractThumbnailFromMediaStore(filePath, range);
                if (checkBitmap(res)) {
                    return res;
                }
                recycle(res);
            }

            // step 2. 从文件tag中读取thumbnail，例如jpeg支持的exif
            {
                res = extractThumbnailFromFileTag(filePath, range);
                if (checkBitmap(res)) {
                    return res;
                }
                recycle(res);
            }

            // step 3. 直接decode文件，尝试从文件中读取
            res = decodeFile(filePath, range, options);
            if (checkBitmap(res)) {
                return res;
            }
            recycle(res);
            return null;
        }

        protected static Bitmap resize(byte[] data, int offset, int length, SizeRange range) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeByteArray(data, offset, length, options);
            if (!checkOptions(options)) {
                return null;
            }
            final int width = options.outWidth;
            final int height = options.outHeight;
            options.inJustDecodeBounds = false;
            options.inSampleSize = computeInSample(width, height, range);
            Bitmap res = BitmapFactory.decodeByteArray(data, offset, length, options);
            if (!checkBitmap(res)) {
                return null;
            }

            return resize(res, range, options);
        }

        protected static Bitmap resize(Bitmap source, SizeRange range, BitmapFactory.Options options) {
            if (!checkBitmap(source)) {
                return source;
            }
            if (null == options) {
                options = new BitmapFactory.Options();
            }

            float scale = computeScale(source.getWidth(), source.getHeight(), range, true);
            if (MathUtils.equals(scale, 1.0F, 0.1F)) {
                return source;
            }

            Bitmap res = BitmapUtils.extractBitmap(source, scale);
            if (res == source) {
                return res;
            }
            recycle(source);
            return res;
        }

        protected static Bitmap resize(File imageFile, SizeRange range) {
            BitmapFactory.Options options = new BitmapFactory.Options();
            options.inJustDecodeBounds = true;
            options.inSampleSize = 1;
            BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            if (!checkOptions(options)) {
                return null;
            }
            options.inJustDecodeBounds = false;
            options.inSampleSize = computeInSample(options.outWidth, options.outHeight, range);
            Bitmap res = BitmapFactory.decodeFile(imageFile.getAbsolutePath(), options);
            if (!checkBitmap(res)) {
                return null;
            }

            return resize(res, range, options);
        }

        protected static float computeScale(int originalWidth, int originalHeight, final SizeRange range, final boolean zoom) {
            if (originalWidth <= 0 || originalHeight <= 0) {
                return 0.0F;
            }

            if (range.withInInternal(originalWidth, originalHeight)) {
                // 1. 宽高都命中区域,  不用scale
                return 1.0F;
            }

            // 2. 宽高都没有命中之都小于目标期望值, 返回当前source, 不用scale
            if (originalWidth <= range.minWidth && originalHeight <= range.minHeight) {
                return 1.0F;
            }

            // 3. 宽高都没有命中之都大于目标期望值, 需要scale
            if (originalWidth >= range.maxWidth && originalHeight >= range.maxHeight) {
                float ws = range.expectWidth / (float) originalWidth;
                float hs = range.expectHeight / (float) originalHeight;
                return ws > hs ? hs : ws;
            }

            // 4. 宽高有一个命中，另一个没有命中
            // 4.1 一个在区间，另一个小于最小值, 不用scale
            if (originalWidth <= range.minWidth || originalHeight <= range.minHeight) {
                return 1.0F;
            }
            // 4.2 一个在区间，另一个大于最大值, 需要scale
            if (originalWidth >= range.maxWidth) {
                return range.maxWidth / (float) originalWidth;
            } else {
                // originalHeight > range.maxHeight
                return range.minHeight / (float) originalHeight;
            }
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

        protected final static class SizeRange {
            protected int expectWidth;
            protected int expectHeight;
            protected int minWidth;
            protected int minHeight;
            protected int maxWidth;
            protected int maxHeight;

            protected boolean withInInternal(int width, int height) {
                if (width >= minWidth && width <= maxWidth
                        && height >= minHeight && height <= maxHeight) {
                    return true;
                }
                return false;
            }
        }
    }

    public static class ImageThumbnailUtils extends BaseThumbnailUtils {
        public final static ImageThumbnailUtils DEFAULT = new ImageThumbnailUtils(true, 0.2F, 0.2F);

        public ImageThumbnailUtils(boolean zoomImage, float upperLimitFactor, float lowerLimitFactor) {
            super(zoomImage, upperLimitFactor, lowerLimitFactor);
        }

        protected Uri getExternalUri() {
            return MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
        }

        protected Uri getThumbnailUri() {
            return MediaStore.Images.Thumbnails.EXTERNAL_CONTENT_URI;
        }

        @Override
        protected Bitmap extractThumbnailFromFileTag(String path, SizeRange range) {
            MimeType mimeType = MimeType.createFromFileName(path);
            if (null == mimeType || !mimeType.getContentType().contains("image/jpeg")) {
                return null;
            }
            ExifInterface exif = null;
            byte[] thumbData = null;
            try {
                exif = new ExifInterface(path);
                thumbData = exif.getThumbnail();
                if (thumbData == null || thumbData.length <= 0) {
                    return null;
                }
                return resize(thumbData, 0, thumbData.length, range);
            } catch (IOException ex) {
                Log.w(TAG, ex);
                return null;
            }
        }

        @Override
        protected Bitmap decodeFile(String filePath, SizeRange range, BitmapFactory.Options options) {
            return resize(new File(filePath), range);
        }
    }

    public static class VideoThumbnailUtils extends BaseThumbnailUtils {
        public final static VideoThumbnailUtils DEFAULT = new VideoThumbnailUtils(true, 0.2F, 0.2F);

        public VideoThumbnailUtils(boolean zoomImage, float upperLimitFactor, float lowerLimitFactor) {
            super(zoomImage, upperLimitFactor, lowerLimitFactor);
        }

        protected Uri getExternalUri() {
            return MediaStore.Video.Media.EXTERNAL_CONTENT_URI;
        }

        protected Uri getThumbnailUri() {
            return MediaStore.Video.Thumbnails.EXTERNAL_CONTENT_URI;
        }

        @Override
        protected Bitmap extractThumbnailFromFileTag(String path, SizeRange range) {
            return null;
        }

        @Override
        protected Bitmap decodeFile(String filePath, SizeRange range, BitmapFactory.Options options) {
            // from mediaRetriver
            Bitmap res = extractFromMediaMetadataRetriever(filePath);
            if (!checkBitmap(res)) {
                return null;
            }
            return resize(res, range, options);
        }
    }
}
