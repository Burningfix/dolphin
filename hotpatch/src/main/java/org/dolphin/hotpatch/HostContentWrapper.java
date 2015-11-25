package org.dolphin.hotpatch;

import android.content.Context;
import android.content.ContextWrapper;
import android.content.res.AssetManager;
import android.content.res.Resources;
import android.util.Log;

/**
 * Created by hanyanan on 2015/11/25.
 */
public class HostContentWrapper extends ContextWrapper {
    Context hostContext;
    public HostContentWrapper(Context base) {
        super(base);
        hostContext = base;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        hostContext = base;
    }

    @Override
    public AssetManager getAssets() {
        Log.d("ddd", "getAssets");
        return hostContext.getAssets();
    }

    @Override
    public Resources getResources() {
        Log.d("ddd", "getResources");
        return hostContext.getResources();
    }

    @Override
    public void setTheme(int resid) {
        hostContext.setTheme(resid);
    }

    @Override
    public Resources.Theme getTheme() {
        return hostContext.getTheme();
    }

    @Override
    public ClassLoader getClassLoader() {
        Log.d("ddd", "getClassLoader");
        return hostContext.getClassLoader();
    }

    @Override
    public String getPackageResourcePath() {
        String res = hostContext.getPackageResourcePath();
        Log.d("ddd", "getPackageResourcePath " + res);
        return res;
    }

    @Override
    public String getPackageCodePath() {
        String res = hostContext.getPackageCodePath();
        Log.d("ddd", "getPackageCodePath " + res);
        return res;
    }

}
