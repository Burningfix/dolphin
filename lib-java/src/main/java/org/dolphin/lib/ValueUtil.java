package org.dolphin.lib;

/**
 * Created by hanyanan on 2015/10/16.
 */
public class ValueUtil {

    public static boolean isEmpty(CharSequence charSequence){
        if(null == charSequence || charSequence.length() <= 0) return true;
        return false;
    }

    public static long parseLong(String s, long defaultValue) {
        if(isEmpty(s)) return defaultValue;

        try {
            return Long.parseLong(s);
        }catch (Exception ex) {
            return defaultValue;
        }
    }

    public static boolean equalsIgnoreCase(String s1, String s2) {
        if(null == s1 && null == s2) {
            throw new IllegalArgumentException("The input at least has one value is not empty!");
        }

        if(null == s1 || null == s2){
            return false;
        }

        return s1.equalsIgnoreCase(s2);
    }

    public static boolean equals(String s1, String s2) {
        if(null == s1 && null == s2) {
            throw new IllegalArgumentException("The input at least has one value is not empty!");
        }

        if(null == s1 || null == s2){
            return false;
        }

        return s1.equals(s2);
    }


}
