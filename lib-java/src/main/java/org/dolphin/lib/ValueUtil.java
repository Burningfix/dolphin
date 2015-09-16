package org.dolphin.lib;

import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

/**
 * Created by dolphin on 2015/3/13.
 */
public class ValueUtil {

    public static boolean isEmpty(CharSequence charSequence){
        if(null == charSequence || charSequence.length() <= 0) return true;
        return false;
    }

    public static String sha1(String input)  {
        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            return ""+input.hashCode();
        }
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    public static String md5(String inputText) {
        if (inputText == null || "".equals(inputText.trim())) {
            throw new IllegalArgumentException("please input un-null value");
        }

        String encryptText = null;
        try {
            MessageDigest m = MessageDigest.getInstance("md5");
            m.update(inputText.getBytes());
            byte s[] = m.digest();
            return hex(s);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ""+inputText.hashCode();
        }
    }

    public static String md5_16(String inputText) {
        if (inputText == null || "".equals(inputText.trim())) {
            throw new IllegalArgumentException("please input un-null value");
        }

        String encryptText = null;
        try {
            MessageDigest m = MessageDigest.getInstance("md5");
            m.update(inputText.getBytes());
            byte s[] = m.digest();
            return hex(s).substring(8, 24);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
            return ""+inputText.hashCode();
        }
    }

    private static String hex(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1,3));
        }
        return sb.toString();
    }

    public static long parseLong(String s, long defaultValue) {
        if(ValueUtil.isEmpty(s)) return defaultValue;

        try {
            return Long.parseLong(s);
        }catch (Exception ex) {
            return defaultValue;
        }
    }
}
