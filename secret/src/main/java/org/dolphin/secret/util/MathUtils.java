package org.dolphin.secret.util;

/**
 * Created by hanyanan on 2016/4/12.
 */
public class MathUtils {
    public static boolean equals(float v1, float v2) {
        return Math.abs(v1 - v2) <= 0.0001F;
    }

    public static boolean equals(float v1, float v2, float deviation) {
        return Math.abs(v1 - v2) <= Math.abs(deviation);
    }
}
