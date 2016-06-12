package org.dolphin.secret.env;

import android.os.Build;

/**
 * Created by hanyanan on 2016/6/12.
 */
public class DeviceVersionHelper {
    public static boolean large(int versionCode) {
        return Build.VERSION.SDK_INT >= versionCode;
    }


}
