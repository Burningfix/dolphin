package org.dolphin.secret.util;

import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.preference.PreferenceManager;
import android.text.TextUtils;

import org.dolphin.secret.SecretApplication;

/**
 * Created by hanyanan on 2014/10/20.
 */
public class ContextUtils {
    public static int getColor(int c) {
        Context context = SecretApplication.getInstance();
        if (null == context) return Color.WHITE;
        return context.getResources().getColor(c);
    }

    public static SharedPreferences getDefaultSharedPreferences(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context);
    }

    public static int getIntFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            int enterCount = sharedPreferences.getInt(key, 0);
            return enterCount;
        }

        return 0;
    }

    public static int getAndIncreaseFromSharedPreferences(Context context, String key) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            int enterCount = sharedPreferences.getInt(key, 0);
            sharedPreferences.edit().putInt(key, enterCount + 1).apply();
            return enterCount;
        }
        return 0;
    }


    public static int getAndPutIntFromSharedPreferences(Context context, String key, int newValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            int enterCount = sharedPreferences.getInt(key, 0);
            sharedPreferences.edit().putInt(key, newValue).apply();
            return enterCount;
        }

        return newValue;
    }

    public static String getStringFromSharedPreferences(Context context, String key, String defaultValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            String data = sharedPreferences.getString(key, defaultValue);
            return data;
        }

        return defaultValue;
    }

    public static String putStringToSharedPreferences(Context context, String key, String value) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            String data = sharedPreferences.getString(key, value);
            if (!TextUtils.isEmpty(value)) {
                sharedPreferences.edit().putString(key, value).apply();
            } else {
                sharedPreferences.edit().remove(key).apply();
            }
            return data;
        }

        return value;
    }

    // boolean
    public static boolean getBooleanFromSharedPreferences(Context context, String key, boolean defaultV) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            boolean check = sharedPreferences.getBoolean(key, defaultV);
            return check;
        }
        return defaultV;
    }

    public static void putBooleanPutSharedPreferences(Context context, String key, boolean newValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            sharedPreferences.edit().putBoolean(key, newValue).apply();
        }
    }

    public static boolean revertAndGetFromSharedPreferences(Context context, String key, boolean defaultV) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            boolean check = sharedPreferences.getBoolean(key, defaultV);
            sharedPreferences.edit().putBoolean(key, !check).apply();
            check = sharedPreferences.getBoolean(key, defaultV);
            return check;
        }

        return defaultV;
    }

    public static boolean getAndRevertFromSharedPreferences(Context context, String key, boolean defaultV) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            boolean check = sharedPreferences.getBoolean(key, defaultV);
            sharedPreferences.edit().putBoolean(key, !check).apply();
            return check;
        }

        return defaultV;
    }

    public static boolean getAndPutBooleanFromSharedPreferences(Context context, String key, boolean newValue) {
        SharedPreferences sharedPreferences = getDefaultSharedPreferences(context);
        if (null != sharedPreferences) {
            boolean oldV = sharedPreferences.getBoolean(key, false);
            sharedPreferences.edit().putBoolean(key, newValue).commit();
            return oldV;
        }

        return newValue;
    }


    public static boolean isValid(Activity activity) {
        if ((null == activity) || activity.isFinishing()
                || activity.isRestricted()) {
            return false;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR1) {
            if (activity.isDestroyed()) {
                return false;
            }
        }

        return true;
    }
}
