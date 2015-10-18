package org.dolphin.lib;

import javax.crypto.*;
import javax.crypto.spec.SecretKeySpec;

import java.io.UnsupportedEncodingException;
import java.security.InvalidKeyException;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by dolphin on 2015/3/13.
 */
public class SecurityUtil {
    /**
     * 生成sha1签名
     */
    public static String sha1(String input) {
        MessageDigest mDigest = null;
        try {
            mDigest = MessageDigest.getInstance("SHA1");
        } catch (NoSuchAlgorithmException e) {
            return "" + input.hashCode();
        }
        byte[] result = mDigest.digest(input.getBytes());
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < result.length; i++) {
            sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
        }

        return sb.toString();
    }

    /**
     * 生成完整的32位md5签名
     */
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
            return "" + inputText.hashCode();
        }
    }

    /**
     * 生成16位的md5（32位md5前面的中间16位）签名
     */
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
            return "" + inputText.hashCode();
        }
    }

    /**
     * 16进制数组转成字符串
     */
    public static String hex(byte[] arr) {
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < arr.length; ++i) {
            sb.append(Integer.toHexString((arr[i] & 0xFF) | 0x100).substring(1, 3));
        }
        return sb.toString();
    }

    /**
     * 字符串转成16进制数组
     */
    public static byte[] hex(String s) {
        int len = s.length();
        byte[] data = new byte[len / 2];
        for (int i = 0; i < len; i += 2) {
            data[i / 2] = (byte) ((Character.digit(s.charAt(i), 16) << 4) + Character
                    .digit(s.charAt(i + 1), 16));
        }
        return data;
    }

    /**
     * 根据appid、token、lol以及时间戳来生成签名
     */
    public String generateSignature(Object ... params) {
        if (null == params || params.length <= 0) {
            throw new IllegalArgumentException("");
        }
        String signature = null;
        List<String> srcList = new ArrayList<String>();
        for (Object o : params) {
            if (null == o || ValueUtil.isEmpty(o.toString())) {
                continue;
            }
            srcList.add(o.toString());
        }

        // 按照字典序逆序拼接参数
        Collections.sort(srcList);
        Collections.reverse(srcList);
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < srcList.size(); i++) {
            sb.append(srcList.get(i));
        }
        signature = sha1(sb.toString());
        srcList.clear();

        return signature;
    }

    /**
     * 验证签名: <br/>
     * 1.根据appid获取该渠道的token;<br/>
     * 2.根据appid、token、lol以及时间戳计算一次签名;<br/>
     * 3.比较传过来的签名以及计算出的签名是否一致;
     *
     * @return
     */
    public boolean isValid(String signature, Object... params) {
        String calculatedSignature = generateSignature(params);
        return ValueUtil.equals(calculatedSignature, signature);
    }


    /**
     * 加密
     *
     * @param content 待加密内容
     * @param key     加密的密钥
     * @return
     */
    public static String aesEncrypt(String content, String key) {
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(key.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            byte[] byteContent = content.getBytes("utf-8");
            cipher.init(Cipher.ENCRYPT_MODE, secretKeySpec);
            byte[] byteRresult = cipher.doFinal(byteContent);
            StringBuffer sb = new StringBuffer();
            for (int i = 0; i < byteRresult.length; i++) {
                String hex = Integer.toHexString(byteRresult[i] & 0xFF);
                if (hex.length() == 1) {
                    hex = '0' + hex;
                }
                sb.append(hex.toUpperCase());
            }
            return sb.toString();
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }

    /**
     * 解密
     *
     * @param content 待解密内容
     * @param key     解密的密钥
     * @return
     */
    public static String decrypt(String content, String key) {
        if (content.length() < 1)
            return null;
        byte[] byteRresult = new byte[content.length() / 2];
        for (int i = 0; i < content.length() / 2; i++) {
            int high = Integer.parseInt(content.substring(i * 2, i * 2 + 1), 16);
            int low = Integer.parseInt(content.substring(i * 2 + 1, i * 2 + 2), 16);
            byteRresult[i] = (byte) (high * 16 + low);
        }
        try {
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128, new SecureRandom(key.getBytes()));
            SecretKey secretKey = kgen.generateKey();
            byte[] enCodeFormat = secretKey.getEncoded();
            SecretKeySpec secretKeySpec = new SecretKeySpec(enCodeFormat, "AES");
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.DECRYPT_MODE, secretKeySpec);
            byte[] result = cipher.doFinal(byteRresult);
            return new String(result);
        } catch (NoSuchAlgorithmException e) {
            e.printStackTrace();
        } catch (NoSuchPaddingException e) {
            e.printStackTrace();
        } catch (InvalidKeyException e) {
            e.printStackTrace();
        } catch (IllegalBlockSizeException e) {
            e.printStackTrace();
        } catch (BadPaddingException e) {
            e.printStackTrace();
        }
        return null;
    }


    public static void main(String[] argv) {
        String src = "Test";
        String key = "123";
        System.out.println("Raw data: " + src + "\tKey: " + key);
        System.out.println("SHA1 data: " + sha1(src));
        System.out.println("Md5 data: " + md5(src));
        System.out.println("Md5_16 data: " + md5_16(src));
        String aes = aesEncrypt(src, key);
        System.out.println("Aes data: " + aes);
        System.out.println("Aes Decode data: " + decrypt(aes, key));
    }
}
