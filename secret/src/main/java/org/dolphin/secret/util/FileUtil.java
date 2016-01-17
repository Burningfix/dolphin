package org.dolphin.secret.util;

import java.io.DataInput;
import java.io.IOException;

/**
 * Created by yananh on 2016/1/17.
 */
public class FileUtil {
    public static void read(DataInput input, byte[] data) throws IOException {
        input.readFully(data);
    }
}
