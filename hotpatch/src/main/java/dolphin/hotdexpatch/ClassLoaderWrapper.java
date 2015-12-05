package dolphin.hotdexpatch;

import android.util.Log;

import org.dolphin.hotpatch.dex.MainActivity;

import java.util.Stack;

/**
 * Created by hanyanan on 2015/11/26.
 */
public class ClassLoaderWrapper extends ClassLoader {
    private final ClassLoader originalSystemPathClassLoader;
    private final Stack<ClassLoader> extraClassLoaderStack = new Stack<ClassLoader>();
    public ClassLoaderWrapper(ClassLoader originalSystemPathClassLoader) {
        super();
        this.originalSystemPathClassLoader = originalSystemPathClassLoader;
    }

    public void pushExtraClassLoader(ClassLoader classLoader){
        synchronized (this) {
            extraClassLoaderStack.push(classLoader);
        }
    }

    public ClassLoader getDelegateClassLoader(){
        return originalSystemPathClassLoader;
    }
    @Override
    public synchronized Class<?> loadClass(String className) throws ClassNotFoundException {
        Log.d(MainActivity.TAG, "ClassLoaderWrapper loadClass " + className);
        ClassNotFoundException exception = null;
        try {
            Class res = getDelegateClassLoader().loadClass(className);
            if (null != res) return res;
        }catch (ClassNotFoundException e) {
            exception = e;
        }

        Stack<ClassLoader> extraClassLoaderStack = new Stack<ClassLoader>();
        synchronized (this) {
            extraClassLoaderStack.addAll(this.extraClassLoaderStack);
        }

        for(ClassLoader classLoader : extraClassLoaderStack) {
            try {
                Class res = classLoader.loadClass(className);
                if(res != null) return res;
            }catch (ClassNotFoundException e) {
                exception = e;
            }
        }

        if(null != exception) throw exception;
        throw new ClassNotFoundException("Cannot find class " + className);
    }

//    @Override
//    protected Class<?> findClass(String className) throws ClassNotFoundException {
//        Log.d(MainActivity.TAG, "ClassLoaderWrapper findClass " + className);
//        return getDelegateClassLoader().findClass(className);
//    }
}
