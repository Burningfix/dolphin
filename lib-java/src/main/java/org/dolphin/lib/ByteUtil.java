package org.dolphin.lib;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.io.Serializable;

/**
 * Created by hanyanan on 2015/3/5.
 */
public class ByteUtil {
    public static byte[] serializableToBytes(Serializable obj) {
        ByteArrayOutputStream bout = new ByteArrayOutputStream();
        ObjectOutputStream out = null;
        try {
            out = new ObjectOutputStream(bout);
            out.writeObject(obj);
            out.flush();
            byte[] bytes = bout.toByteArray();
            return bytes;
        } catch (IOException e) {
            e.printStackTrace();
            return new byte[0];
        } finally {
            IOUtil.safeClose(bout);
            IOUtil.safeClose(out);
        }
    }

    public static Object bytesToObject(byte[] bytes) {
        ByteArrayInputStream bi = new ByteArrayInputStream(bytes);
        ObjectInputStream oi = null;
        try {
            oi = new ObjectInputStream(bi);
            Object obj = oi.readObject();
            return obj;
        } catch (IOException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } finally {
            IOUtil.safeClose(bi);
            IOUtil.safeClose(oi);
        }
        return null;
    }


    public static byte[] floatToBytes(float f) {
        // 把float转换为byte[]
        int fbit = Float.floatToIntBits(f);

        byte[] b = new byte[4];
        for (int i = 0; i < 4; i++) {
            b[i] = (byte) (fbit >> (24 - i * 8));
        }

        int len = b.length;
        byte[] dest = new byte[len];
        System.arraycopy(b, 0, dest, 0, len);
        byte temp;
        for (int i = 0; i < len / 2; ++i) {
            temp = dest[i];
            dest[i] = dest[len - i - 1];
            dest[len - i - 1] = temp;
        }

        return dest;
    }

    public static float bytesToFloat(byte[] b, int offset) {
        int l;
        l = b[offset + 0];
        l &= 0xff;
        l |= ((long) b[offset + 1] << 8);
        l &= 0xffff;
        l |= ((long) b[offset + 2] << 16);
        l &= 0xffffff;
        l |= ((long) b[offset + 3] << 24);
        return Float.intBitsToFloat(l);
    }

    public static byte[] shortToBytes(short data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data & 0xff00) >> 8);
        return bytes;
    }

    public static byte[] charToBytes(char data) {
        byte[] bytes = new byte[2];
        bytes[0] = (byte) (data);
        bytes[1] = (byte) (data >> 8);
        return bytes;
    }

    public static byte[] longToBytes(long data) {
        byte[] bytes = new byte[8];
        bytes[0] = (byte) (data & 0xff);
        bytes[1] = (byte) ((data >> 8) & 0xff);
        bytes[2] = (byte) ((data >> 16) & 0xff);
        bytes[3] = (byte) ((data >> 24) & 0xff);
        bytes[4] = (byte) ((data >> 32) & 0xff);
        bytes[5] = (byte) ((data >> 40) & 0xff);
        bytes[6] = (byte) ((data >> 48) & 0xff);
        bytes[7] = (byte) ((data >> 56) & 0xff);
        return bytes;
    }

    public static long bytesToLong(byte[] bytes) {
        return bytesToLong(bytes, 0);
    }

    public static long bytesToLong(byte[] bytes, int offset) {
        return (0xffL & (long) bytes[offset])
                | (0xff00L & ((long) bytes[offset + 1] << 8))
                | (0xff0000L & ((long) bytes[offset + 2] << 16))
                | (0xff000000L & ((long) bytes[offset + 3] << 24))
                | (0xff00000000L & ((long) bytes[offset + 4] << 32))
                | (0xff0000000000L & ((long) bytes[offset + 5] << 40))
                | (0xff000000000000L & ((long) bytes[offset + 6] << 48))
                | (0xff00000000000000L & ((long) bytes[offset + 7] << 56));
    }

    public static int bytesToInt(byte[] bytes) {
        return (0xff & bytes[0])
                | (0xff00 & (bytes[1] << 8))
                | (0xff0000 & (bytes[2] << 16))
                | (0xff000000 & (bytes[3] << 24));
    }

    public static byte[] intToBytes(int i) {
        byte[] result = new byte[4];
//        result[0] = (byte) ((i >> 24) & 0xFF);
//        result[1] = (byte) ((i >> 16) & 0xFF);
//        result[2] = (byte) ((i >> 8) & 0xFF);
//        result[3] = (byte) (i & 0xFF);
        result[0] = (byte) (i & 0xff);
        result[1] = (byte) ((i >> 8) & 0xff);
        result[2] = (byte) ((i >> 16) & 0xff);
        result[3] = (byte) ((i >> 24) & 0xff);
        return result;
    }

    public static int bytesToInt(byte[] b, int offset) {
        return (0xff & b[offset])
                | (0xff00 & (b[offset + 1] << 8))
                | (0xff0000 & (b[offset + 2] << 16))
                | (0xff000000 & (b[offset + 3] << 24));
    }

    public static byte[] doubleToBytes(double data) {
        long intBits = Double.doubleToLongBits(data);
        return longToBytes(intBits);
    }

    public static byte[] stringToBytes(String data) {
        return data.getBytes();
    }

    public static short byteArrayToShort(byte[] bytes) {
        return (short) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }

    public static char bytesToChar(byte[] bytes) {
        return (char) ((0xff & bytes[0]) | (0xff00 & (bytes[1] << 8)));
    }


    public static float bytesToFloat(byte[] bytes) {
        return Float.intBitsToFloat(bytesToInt(bytes));
    }

    public static double bytesToDouble(byte[] bytes) {
        long l = bytesToLong(bytes);
        return Double.longBitsToDouble(l);
    }

    public static String bytesToString(byte[] bytes) {
        return new String(bytes);
    }
}
