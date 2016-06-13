package org.dolphin.lib.util;

/**
 * Created by hanyanan on 2015/10/16.
 */
public class ValueUtil {

    public static boolean isEmpty(CharSequence charSequence) {
        if (null == charSequence || charSequence.length() <= 0) return true;
        return false;
    }

    public static boolean isEmpty(Object[] data) {
        return null == data || data.length <= 0;
    }

    public static boolean isEquals(Object obj1, Object obj2) {
        if (obj1 == obj2) return true;
        if (null == obj1 || null == obj2) return false;
        return obj1.equals(obj2);
    }

    public static long parseLong(String s, long defaultValue) {
        if (isEmpty(s)) return defaultValue;

        try {
            return Long.valueOf(s);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static int parseInt(String s, int defaultValue) {
        if (isEmpty(s)) return defaultValue;
        try {
            return Integer.valueOf(s);
        } catch (Exception ex) {
            return defaultValue;
        }
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        if (null == s1 && null == s2) {
            throw new IllegalArgumentException("The input at least has one value is not empty!");
        }

        if (null == s1 || null == s2) {
            return false;
        }

        return s1.equalsIgnoreCase(s2);
    }

    public static boolean equals(String s1, String s2) {
        if (null == s1 && null == s2) {
            throw new IllegalArgumentException("The input at least has one value is not empty!");
        }

        if (null == s1 || null == s2) {
            return false;
        }

        return s1.equals(s2);
    }


}
