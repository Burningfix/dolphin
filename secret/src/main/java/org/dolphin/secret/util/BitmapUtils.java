package org.dolphin.secret.util;

import android.graphics.Bitmap;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class BitmapUtils {

    public static void recycle(Bitmap bitmap){
        if(null == bitmap || bitmap.isRecycled()) return ;
        bitmap.recycle();
    }

}
