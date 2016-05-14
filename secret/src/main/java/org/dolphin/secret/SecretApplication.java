package org.dolphin.secret;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;

import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.http.HttpServer;

/**
 * Created by hanyanan on 2016/1/26.
 */
public class SecretApplication extends Application {
    private static SecretApplication instance = null;

    public static SecretApplication getInstance() {
        return instance;
    }

    private final HttpServer httpServer = new HttpServer();
    private int widthPixels = -1, heightPixels = -1;

    @Override
    public void onCreate() {
        super.onCreate();
        BrowserManager.getInstance().startScan();
        httpServer.start();
    }

    @Override
    public void onTerminate() {
        super.onTerminate();
    }

    public SecretApplication instance() {
        return instance;
    }

    public final HttpServer getHttpServer() {
        return httpServer;
    }


    public int getWidth() {
        if (widthPixels <= 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            widthPixels = dm.widthPixels;
            heightPixels = dm.heightPixels;
        }
        return widthPixels;
    }

    public int getHeight() {
        if (widthPixels <= 0) {
            DisplayMetrics dm = getResources().getDisplayMetrics();
            widthPixels = dm.widthPixels;
            heightPixels = dm.heightPixels;
        }
        return heightPixels;
    }

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }
}
