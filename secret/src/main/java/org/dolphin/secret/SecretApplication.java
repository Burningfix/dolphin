package org.dolphin.secret;

import android.app.Application;
import android.content.Context;

import org.dolphin.secret.browser.BrowserManager;

import java.io.File;

/**
 * Created by hanyanan on 2016/1/26.
 */
public class SecretApplication extends Application {
    private static SecretApplication instance = null;
    public static SecretApplication getInstance(){
        return instance;
    }



    @Override
    public void onCreate() {
        super.onCreate();
        BrowserManager.getInstance().startScan();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }
}
