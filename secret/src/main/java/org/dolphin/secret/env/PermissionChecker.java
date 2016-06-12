package org.dolphin.secret.env;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.content.ContextCompat;

import org.dolphin.lib.util.ValueUtil;

/**
 * Created by hanyanan on 2016/6/12.
 */
public class PermissionChecker {
    // http://gudong.name/%E6%8A%80%E6%9C%AF/2015/11/10/android_m_permission.html
    public static boolean checkRunningEnvironment(Context base) {
        return check(base, Manifest.permission.WRITE_EXTERNAL_STORAGE)
                && check(base, Manifest.permission.READ_EXTERNAL_STORAGE);
    }


    public static boolean check(Context context, String permission) {
        if (ValueUtil.isEmpty(permission)) {
            return false;
        }

        if (Manifest.permission.WRITE_EXTERNAL_STORAGE.equals(permission)) {
            return checkCommonPermission(context, permission);
        }


        if (DeviceVersionHelper.large(Build.VERSION_CODES.JELLY_BEAN)
                && Manifest.permission.READ_EXTERNAL_STORAGE.equals(permission)) {
            return checkCommonPermission(context, permission);
        }

//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
//            int selfPermission = ContextCompat.checkSelfPermission(context, permission);
//            return selfPermission == PackageManager.PERMISSION_GRANTED;
//        }

        return checkCommonPermission(context, permission);
    }


    private static boolean checkCommonPermission(Context context, String permission) {
        int selfPermission = ContextCompat.checkSelfPermission(context, permission);
        return selfPermission == PackageManager.PERMISSION_GRANTED;
    }

    public static boolean checkWriteExternalPermission() {
        return true;
    }
}
