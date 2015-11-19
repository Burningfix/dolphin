package org.dolphin.http.server.dexhotpatch;

import java.io.File;

/**
 * Created by hanyanan on 2015/11/18.
 */
public class TestDexCode {
    static String DEX_SUFFIX = ".dex";

    static String optimizedPathFor(File path, File optimizedDirectory) {
        /*
         * Get the filename component of the path, and replace the
         * suffix with ".dex" if that's not already the suffix.
         *
         * We don't want to use ".odex", because the build system uses
         * that for files that are paired with resource-only jar
         * files. If the VM can assume that there's no classes.dex in
         * the matching jar, it doesn't need to open the jar to check
         * for updated dependencies, providing a slight performance
         * boost at startup. The use of ".dex" here matches the use on
         * files in /data/dalvik-cache.
         */
        String fileName = path.getName();
        if (!fileName.endsWith(DEX_SUFFIX)) {
            int lastDot = fileName.lastIndexOf(".");
            if (lastDot < 0) {
                fileName += DEX_SUFFIX;
            } else {
                StringBuilder sb = new StringBuilder(lastDot + 4);
                sb.append(fileName, 0, lastDot);
                sb.append(DEX_SUFFIX);
                fileName = sb.toString();
            }
        }

        File result = new File(optimizedDirectory, fileName);
        return result.getPath();
    }


    public static void main(String[] argv) {
        System.out.println(optimizedPathFor(new File("D:\\multidex\\classes.dex"), new File("D:\\multidex")));
        System.out.println(optimizedPathFor(new File("D:\\multidex\\classes.jar"), new File("D:\\multidex")));
    }
}
