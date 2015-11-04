package org.dolphin.job.util;


/**
 * Created by hanyanan on 2015/9/22.
 */
public class Log {
    private static final org.dolphin.lib.Log INNER_LOG = new org.dolphin.lib.Log(System.out, System.out, System.out,
            System.err, System.err);


    public static void d(String tag, String msg){
        INNER_LOG.d(tag, msg);
    }

    public static void v(String tag, String msg){
        INNER_LOG.v(tag, msg);
    }

    public static void i(String tag, String msg){
        INNER_LOG.i(tag, msg);
    }

    public static void w(String tag, String msg){
        INNER_LOG.w(tag, msg);
    }

    public static void e(String tag, String msg){
        INNER_LOG.e(tag, msg);
    }
}
