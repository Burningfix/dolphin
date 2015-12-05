package org.dolphin.hotpatch.dex;

import android.app.Application;
import android.content.Context;
import android.os.Handler;
import android.util.Log;

import org.dolphin.lib.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;
import dolphin.hotdexpatch.ClassLoaderWrapper;
import dolphin.hotdexpatch.MyPathClassLoader;

/**
 * Created by hanyanan on 2015/11/16.
 */
public class DexLoadApplication extends Application {
    private DexHotPatchEngine dexHotPatchEngine;

    public DexLoadApplication() {
        super();
    }

    @Override
    protected void attachBaseContext(Context base) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        try {
            Field loadedApkField = ReflectUtil.findField(base, "mPackageInfo");
            Object loadedApk = loadedApkField.get(base);
            Field  mClassLoader = ReflectUtil.findField(loadedApk, "mClassLoader");
            ClassLoader classLoader = (ClassLoader) mClassLoader.get(loadedApk);
            ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper(classLoader);
            ClassLoader defaultClassLoader = classLoaderWrapper.getClass().getClassLoader();
            Log.d("ClassLoader","Default ClassLoader " + defaultClassLoader);
            Log.d("ClassLoader","System ClassLoader " + systemClassLoader);
            Log.d("ClassLoader", "Normal ClassLoader " + classLoader);
            long t1 = System.currentTimeMillis();
            MyPathClassLoader myPathClassLoader = new MyPathClassLoader(base, (PathClassLoader) classLoader);
            long t2 = System.currentTimeMillis();
            Log.d("ClassLoader","Create MyPathClassLoader Cost " + (t2 - t1));
            mClassLoader.set(loadedApk, myPathClassLoader);
//            mClassLoader.set(loadedApk, classLoaderWrapper);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }

        super.attachBaseContext(base);
        dexHotPatchEngine = DexHotPatchEngine.instance(this);
        dexHotPatchEngine.attachToApplication();
//        new Handler(getMainLooper()).post(new Runnable() {
//            @Override
//            public void run() {
//                displayLoadedApk();
//            }
//        });

    }


//    public void attach(Context context) {
//        Log.e("ClassLoader", "attach");
//        Class<?> clazz = Application.class;
//        Method method = null;
//        try {
//            method = clazz.getDeclaredMethod("attach", Context.class);
//            if (!method.isAccessible()) {
//                method.setAccessible(true);
//            }
//            method.invoke(this, context);
//        } catch (NoSuchMethodException e) {
//            e.printStackTrace();
//        } catch (InvocationTargetException e) {
//            e.printStackTrace();
//        } catch (IllegalAccessException e) {
//            e.printStackTrace();
//        }
//    }


    private void displayLoadedApk() {
        try {
            Field loadedApkField = ReflectUtil.findField(this, "mLoadedApk");
            Object loadedApk = loadedApkField.get(this);
            Log.d("ClassLoader", "DexLoadApplication loadedApk " + loadedApk);

            Field  mClassLoader = ReflectUtil.findField(loadedApk, "mClassLoader");
            ClassLoader classLoader = (ClassLoader) mClassLoader.get(loadedApk);
            ClassLoaderWrapper classLoaderWrapper = new ClassLoaderWrapper(classLoader);
            mClassLoader.set(loadedApk, classLoaderWrapper) ;

        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }
    }
}
