package org.dolphin.arch;

import android.os.Looper;

import org.dolphin.lib.Preconditions;

/**
 * Created by hanyanan on 2016/1/11.
 */
public class Util {
    public static void checkThreadState(){
        Preconditions.checkState(Looper.getMainLooper() != Looper.myLooper(), "Forbiden in un-ui thread!");
    }
}
