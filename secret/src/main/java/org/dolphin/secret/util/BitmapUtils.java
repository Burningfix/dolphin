package org.dolphin.secret.util;

import android.graphics.Bitmap;
import android.graphics.BitmapFactory;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class BitmapUtils {

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

}
