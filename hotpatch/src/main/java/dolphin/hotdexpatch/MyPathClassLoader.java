package dolphin.hotdexpatch;

import android.app.Application;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.util.Log;

import java.io.File;

import dalvik.system.PathClassLoader;

/**
 * Created by hanyanan on 2015/12/1.
 */
public class MyPathClassLoader extends PathClassLoader {
    public MyPathClassLoader(Context context, PathClassLoader pathClassLoader) {
        super(context.getApplicationInfo().sourceDir, context.getApplicationInfo().dataDir + File.separator + "lib", pathClassLoader.getParent());
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Log.d("ClassLoader", "LoadClass " + className);
        return super.loadClass(className);
    }
}
