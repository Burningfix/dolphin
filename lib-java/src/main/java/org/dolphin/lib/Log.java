package org.dolphin.lib;

import java.io.PrintStream;

/**
 * Created by hanyanan on 2015/9/15.
 */
public class Log {
    private final PrintStream vLevelLogOut;
    private final PrintStream dLevelLogOut;
    private final PrintStream iLevelLogOut;
    private final PrintStream wLevelLogOut;
    private final PrintStream eLevelLogOut;

    public Log(PrintStream vLevelLogOut, PrintStream dLevelLogOut, PrintStream iLevelLogOut,
               PrintStream wLevelLogOut, PrintStream eLevelLogOut) {
        this.vLevelLogOut = vLevelLogOut;
        this.dLevelLogOut = dLevelLogOut;
        this.iLevelLogOut = iLevelLogOut;
        this.wLevelLogOut = wLevelLogOut;
        this.eLevelLogOut = eLevelLogOut;
    }

    public void d(String tag, String msg){
        if(null != dLevelLogOut) {
            printLog(dLevelLogOut, "D", tag, msg);
        }
    }

    public void v(String tag, String msg){
        if(null != vLevelLogOut) {
            printLog(vLevelLogOut, "V", tag, msg);
        }
    }

    public void i(String tag, String msg){
        if(null != iLevelLogOut) {
            printLog(iLevelLogOut, "I", tag, msg);
        }
    }

    public void w(String tag, String msg){
        if(null != wLevelLogOut) {
            printLog(wLevelLogOut, "W", tag, msg);
        }
    }

    public void e(String tag, String msg){
        if(null != eLevelLogOut) {
            printLog(eLevelLogOut, "E", tag, msg);
        }
    }

    public static void printLog(PrintStream printStream, String level, String tag, String msg){
        if(null != printStream) {
            String logTime = DateUtils.getCurrentTime();
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append("Thread-").append(Thread.currentThread().getId())
                    .append(' ')
                    .append(logTime)
                    .append(' ')
                    .append(level)
                    .append('/')
                    .append(tag)
                    .append(":\t")
                    .append(msg);
            printStream.println(stringBuilder.toString());
        }
    }
}
