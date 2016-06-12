package org.dolphin.secret;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.os.Looper;
import android.util.DisplayMetrics;
import android.widget.Toast;

import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.env.PermissionActivity;
import org.dolphin.secret.env.PermissionDeniedException;
import org.dolphin.secret.env.PermissionGrantedException;
import org.dolphin.secret.env.PermissionProcessor;
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
        if (PermissionProcessor.checkRunningPermission(this)) {
            Toast.makeText(this, "check permission true", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(this, "check permission false", Toast.LENGTH_SHORT).show();
            Intent intent = new Intent(this, PermissionActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            try {
                Looper.loop();
            } catch (PermissionGrantedException exception) {
                exception.printStackTrace();
            } catch (PermissionDeniedException ex) {
                ex.printStackTrace();
                android.os.Process.killProcess(android.os.Process.myPid());
                System.exit(1);
            }
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

    @Override
    protected void attachBaseContext(Context base) {
        super.attachBaseContext(base);
        instance = this;
    }
}
