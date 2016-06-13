package org.dolphin.secret;

import android.app.Application;
import android.content.Context;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.permission.PermissionProcessor;
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
    public int checkSelfPermission(String permission) {
        return super.checkSelfPermission(permission);
    }


    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }

    @Override
    public void onCreate() {
        super.onCreate();

        if (PermissionProcessor.makeSurePermissionsAllowed(this)) {
            Toast.makeText(this, "check permission true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "check permission false", Toast.LENGTH_SHORT).show();
            android.os.Process.killProcess(android.os.Process.myPid());
            System.exit(0);
        }

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
}
