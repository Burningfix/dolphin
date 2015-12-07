package dolphin.hotdexpatch;

import android.app.Application;
import android.content.Context;
import android.content.ContextWrapper;
import android.os.Handler;
import android.util.Log;

import org.dolphin.lib.ReflectUtil;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import dalvik.system.PathClassLoader;

/**
 * Created by hanyanan on 2015/11/16.
 */
public class ProxyApplication extends Application {

    public ProxyApplication() {
        super();
        MyPathClassLoader.printCurrClassLoader("DexLoadApplication.DexLoadApplication");
    }


    @Override
    protected void attachBaseContext(Context base) {
        ClassLoader systemClassLoader = ClassLoader.getSystemClassLoader();
        Application dexLoadApplication = null;
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
            Log.d("ClassLoader", "Create MyPathClassLoader Cost " + (t2 - t1));
            mClassLoader.set(loadedApk, myPathClassLoader);
//            mClassLoader.set(loadedApk, classLoaderWrapper);
            Class clazz = Class.forName("dolphin.hotdexpatch.DelegateApplication", true, myPathClassLoader);
            dexLoadApplication = (Application) clazz.newInstance();
            try {
                Method method = ReflectUtil.findMethod(dexLoadApplication, "attachBaseContext", Context.class);
                method.invoke(dexLoadApplication, base);
            } catch (NoSuchMethodException e) {
                e.printStackTrace();
            } catch (InvocationTargetException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            }


            Log.d("Context", "Application's attachBaseContext  =  " + base);
            Log.d("Context","LoadedApk's ClassLoader =  " + classLoader);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        }

        super.attachBaseContext(base);
        try {
            Field loadedApkField = ReflectUtil.findField(dexLoadApplication, "mLoadedApk");
            Context nextContext;
            Context context = base;
            while ((context instanceof ContextWrapper) &&
                    (nextContext=((ContextWrapper)context).getBaseContext()) != null) {
                context = nextContext;
            }
            Field realLoadedApkField = ReflectUtil.findField(context, "mPackageInfo");
            Object loadedApk = realLoadedApkField.get(context);
            loadedApkField.set(dexLoadApplication, loadedApk);
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
        } catch (IllegalAccessException e) {
            e.printStackTrace();
        }


        MyPathClassLoader.printCurrClassLoader("DexLoadApplication.attachBaseContext");




        new Handler(getMainLooper()).post(new Runnable() {
            @Override
            public void run() {
                Log.d("Context", "Application's getBaseContext  =  " + getBaseContext());
                Log.d("Context", "Application's getApplicationContext  =  " + getApplicationContext());
                Log.d("Context", "Application's ClassLoader =  " + getClassLoader());
//                displayLoadedApk();
            }
        });

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
