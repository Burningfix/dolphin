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
    public static void printCurrClassLoader(String msg){
        String s = new String("");
        Log.d("ClassLoader",msg + " ClassLoader " + s.getClass().getClassLoader());
    }
    private PathClassLoader pathClassLoader;
    public MyPathClassLoader(Context context, PathClassLoader pathClassLoader) {
        super(context.getApplicationInfo().sourceDir, context.getApplicationInfo().dataDir + File.separator + "lib",  pathClassLoader.getParent());
        this.pathClassLoader = pathClassLoader;
    }

    @Override
    public Class<?> loadClass(String className) throws ClassNotFoundException {
        Log.d("ClassLoader", "LoadClass " + className);
        return super.loadClass(className);
//        ClassNotFoundException resE;
//        try {
//            Class clazz = pathClassLoader.loadClass(className);
//            if(null != clazz) {
//                return clazz;
//            }
//        }catch (ClassNotFoundException exception){
//            resE = exception;
//        }
//
//
//        try {
//            Class clazz = super.loadClass(className);
//            if(null != clazz) {
//                return clazz;
//            }
//        }catch (ClassNotFoundException exception){
//            throw exception;
//        }
//
//
//        throw new ClassNotFoundException();
    }

//    @Override
//    protected Class<?> loadClass(String className, boolean resolve) throws ClassNotFoundException {
//        Log.d("ClassLoader", "LoadClass " + className);
//
//        this.pathClassLoader.loadClass()
//
//
//
//
//
//        return super.loadClass(className, resolve);
//    }
}
