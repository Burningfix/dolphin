package dolphin.hotdexpatch;

import android.app.Application;
import android.content.Context;

import org.dolphin.hotpatch.dex.DexHotPatchEngine;

/**
 * Created by yananh on 2015/12/6.
 */
public class LoaderApplication extends Application {
    private DexHotPatchEngine dexHotPatchEngine;
    private static Application dexLoadApplication;

    public synchronized static Application instance(){
        if(null == dexLoadApplication) {
            throw new RuntimeException("instance is null!");
        }

        return dexLoadApplication;
    }

    public LoaderApplication() {
        super();
        dexLoadApplication = this;
        MyPathClassLoader.printCurrClassLoader("DelegateApplication.DelegateApplication");
    }

    @Override
    public void onCreate() {
        super.onCreate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);

        dexHotPatchEngine = DexHotPatchEngine.instance(this);
        dexHotPatchEngine.attachToApplication();
        MyPathClassLoader.printCurrClassLoader("DelegateApplication.attachBaseContext");
    }
}
