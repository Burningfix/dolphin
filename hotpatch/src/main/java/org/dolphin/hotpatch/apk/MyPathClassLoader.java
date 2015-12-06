package org.dolphin.hotpatch.apk;

import android.content.Context;
import android.util.Log;

import java.io.File;

import dalvik.system.PathClassLoader;

/**
 * Created by yananh on 2015/11/28.
 */
public class MyPathClassLoader extends PathClassLoader {
    public MyPathClassLoader(Context context){
        super(context.getApplicationInfo().sourceDir, context.getApplicationInfo().dataDir + File.separator + "lib", context.getClassLoader().getParent());
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Log.d("ClassLoader", "Load class " + className);
        return super.loadClass(className);
    }
}
