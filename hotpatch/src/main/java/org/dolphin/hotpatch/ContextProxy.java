package org.dolphin.hotpatch;

import android.annotation.TargetApi;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.IntentSender;
import android.content.ServiceConnection;
import android.content.SharedPreferences;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.content.res.AssetManager;
import android.content.res.Configuration;
import android.content.res.Resources;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.os.UserHandle;
import android.support.annotation.Nullable;
import android.util.Log;
import android.view.Display;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * Created by hanyanan on 2015/11/25.
 */
public class ContextProxy extends Context {
    private Context hostContext = null;
    public ContextProxy(Context base) {
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

    //-----------------------------------------------------------------------------------------------------

    @Override
    public Context createPackageContext(String packageName, int flags) throws PackageManager.NameNotFoundException {
        return hostContext.createPackageContext(packageName, flags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Context createConfigurationContext(Configuration overrideConfiguration) {
        return hostContext.createConfigurationContext(overrideConfiguration);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public Context createDisplayContext(Display display) {
        return hostContext.createDisplayContext(display);
    }

    @Override
    public boolean isRestricted() {
        return hostContext.isRestricted();
    }

    @Override
    public PackageManager getPackageManager() {
        return hostContext.getPackageManager();
    }

    @Override
    public ContentResolver getContentResolver() {
        return hostContext.getContentResolver();
    }

    @Override
    public Looper getMainLooper() {
        return hostContext.getMainLooper();
    }

    @Override
    public Context getApplicationContext() {
        return hostContext.getApplicationContext();
    }

    @Override
    public String getPackageName() {
        return hostContext.getPackageName();
    }

    @Override
    public ApplicationInfo getApplicationInfo() {
        return hostContext.getApplicationInfo();
    }

    @Override
    public SharedPreferences getSharedPreferences(String name, int mode) {
        return hostContext.getSharedPreferences(name, mode);
    }

    @Override
    public FileInputStream openFileInput(String name) throws FileNotFoundException {
        return hostContext.openFileInput(name);
    }

    @Override
    public FileOutputStream openFileOutput(String name, int mode) throws FileNotFoundException {
        return hostContext.openFileOutput(name, mode);
    }

    @Override
    public boolean deleteFile(String name) {
        return hostContext.deleteFile(name);
    }

    @Override
    public File getFileStreamPath(String name) {
        return hostContext.getFileStreamPath(name);
    }

    @Override
    public File getFilesDir() {
        return hostContext.getFilesDir();
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    @Override
    public File getNoBackupFilesDir() {
        return hostContext.getNoBackupFilesDir();
    }

    @Nullable
    @Override
    public File getExternalFilesDir(String type) {
        return hostContext.getExternalFilesDir(type);
    }

    @Override
    public File[] getExternalFilesDirs(String type) {
        return hostContext.getExternalFilesDirs(type);
    }

    @Override
    public File getObbDir() {
        return hostContext.getObbDir();
    }

    @Override
    public File[] getObbDirs() {
        return hostContext.getObbDirs();
    }

    @Override
    public File getCacheDir() {
        return hostContext.getCacheDir();
    }

    @Override
    public File getCodeCacheDir() {
        return hostContext.getCodeCacheDir();
    }

    @Nullable
    @Override
    public File getExternalCacheDir() {
        return hostContext.getExternalCacheDir();
    }

    @Override
    public File[] getExternalCacheDirs() {
        return hostContext.getExternalCacheDirs();
    }

    @Override
    public File[] getExternalMediaDirs() {
        return hostContext.getExternalMediaDirs();
    }

    @Override
    public String[] fileList() {
        return hostContext.fileList();
    }

    @Override
    public File getDir(String name, int mode) {
        return hostContext.getDir(name, mode);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory) {
        return hostContext.openOrCreateDatabase(name, mode, factory);
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode, SQLiteDatabase.CursorFactory factory, DatabaseErrorHandler errorHandler) {
        return hostContext.openOrCreateDatabase(name, mode, factory, errorHandler);
    }

    @Override
    public boolean deleteDatabase(String name) {
        return hostContext.deleteFile(name);
    }

    @Override
    public File getDatabasePath(String name) {
        return hostContext.getDatabasePath(name);
    }

    @Override
    public String[] databaseList() {
        return hostContext.databaseList();
    }

    @Override
    public Drawable getWallpaper() {
        return hostContext.getWallpaper();
    }

    @Override
    public Drawable peekWallpaper() {
        return hostContext.peekWallpaper();
    }

    @Override
    public int getWallpaperDesiredMinimumWidth() {
        return hostContext.getWallpaperDesiredMinimumWidth();
    }

    @Override
    public int getWallpaperDesiredMinimumHeight() {
        return hostContext.getWallpaperDesiredMinimumHeight();
    }

    @Override
    public void setWallpaper(Bitmap bitmap) throws IOException {
        hostContext.setWallpaper(bitmap);
    }

    @Override
    public void setWallpaper(InputStream data) throws IOException {
        hostContext.setWallpaper(data);
    }

    @Override
    public void clearWallpaper() throws IOException {
        hostContext.clearWallpaper();
    }

    @Override
    public void startActivity(Intent intent) {
        hostContext.startActivity(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivity(Intent intent, Bundle options) {
        hostContext.startActivity(intent, options);
    }

    @Override
    public void startActivities(Intent[] intents) {
        hostContext.startActivities(intents);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startActivities(Intent[] intents, Bundle options) {
        hostContext.startActivities(intents, options);
    }

    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags) throws IntentSender.SendIntentException {
        hostContext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void startIntentSender(IntentSender intent, Intent fillInIntent, int flagsMask, int flagsValues, int extraFlags, Bundle options) throws IntentSender.SendIntentException {
        hostContext.startIntentSender(intent, fillInIntent, flagsMask, flagsValues, extraFlags, options);
    }

    @Override
    public void sendBroadcast(Intent intent) {
        hostContext.sendBroadcast(intent);
    }

    @Override
    public void sendBroadcast(Intent intent, String receiverPermission) {
        hostContext.sendBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission) {
        hostContext.sendBroadcast(intent, receiverPermission);
    }

    @Override
    public void sendOrderedBroadcast(Intent intent, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        hostContext.sendOrderedBroadcast(intent, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user) {
        hostContext.sendBroadcastAsUser(intent, user);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission) {
        hostContext.sendBroadcastAsUser(intent, user, receiverPermission);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendOrderedBroadcastAsUser(Intent intent, UserHandle user, String receiverPermission, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        hostContext.sendOrderedBroadcastAsUser(intent, user, receiverPermission, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override
    public void sendStickyBroadcast(Intent intent) {
        hostContext.sendStickyBroadcast(intent);
    }

    @Override
    public void sendStickyOrderedBroadcast(Intent intent, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        hostContext.sendStickyOrderedBroadcast(intent, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @Override
    public void removeStickyBroadcast(Intent intent) {
        hostContext.removeStickyBroadcast(intent);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendStickyBroadcastAsUser(Intent intent, UserHandle user) {
        hostContext.sendBroadcastAsUser(intent, user);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void sendStickyOrderedBroadcastAsUser(Intent intent, UserHandle user, BroadcastReceiver resultReceiver, Handler scheduler, int initialCode, String initialData, Bundle initialExtras) {
        hostContext.sendStickyOrderedBroadcastAsUser(intent, user, resultReceiver, scheduler, initialCode, initialData, initialExtras);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN_MR1)
    @Override
    public void removeStickyBroadcastAsUser(Intent intent, UserHandle user) {
        hostContext.removeStickyBroadcastAsUser(intent, user);
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter) {
        return hostContext.registerReceiver(receiver, filter);
    }

    @Nullable
    @Override
    public Intent registerReceiver(BroadcastReceiver receiver, IntentFilter filter, String broadcastPermission, Handler scheduler) {
        return hostContext.registerReceiver(receiver, filter, broadcastPermission, scheduler);
    }

    @Override
    public void unregisterReceiver(BroadcastReceiver receiver) {
        hostContext.unregisterReceiver(receiver);
    }

    @Nullable
    @Override
    public ComponentName startService(Intent service) {
        return hostContext.startService(service);
    }

    @Override
    public boolean stopService(Intent service) {
        return hostContext.stopService(service);
    }

    @Override
    public boolean bindService(Intent service, ServiceConnection conn, int flags) {
        return hostContext.bindService(service, conn, flags);
    }

    @Override
    public void unbindService(ServiceConnection conn) {
        hostContext.unbindService(conn);
    }

    @Override
    public boolean startInstrumentation(ComponentName className, String profileFile, Bundle arguments) {
        return hostContext.startInstrumentation(className, profileFile, arguments);
    }

    @Override
    public Object getSystemService(String name) {
        return hostContext.getSystemService(name);
    }

    @Override
    public String getSystemServiceName(Class<?> serviceClass) {
        return hostContext.getSystemServiceName(serviceClass);
    }

    @Override
    public int checkPermission(String permission, int pid, int uid) {
        return hostContext.checkPermission(permission, pid, uid);
    }

    @Override
    public int checkCallingPermission(String permission) {
        return hostContext.checkCallingPermission(permission);
    }

    @Override
    public int checkCallingOrSelfPermission(String permission) {
        return hostContext.checkCallingOrSelfPermission(permission);
    }

    @Override
    public int checkSelfPermission(String permission) {
        return hostContext.checkSelfPermission(permission);
    }

    @Override
    public void enforcePermission(String permission, int pid, int uid, String message) {
        hostContext.enforcePermission(permission, pid, uid, message);
    }

    @Override
    public void enforceCallingPermission(String permission, String message) {
        hostContext.enforceCallingPermission(permission, message);
    }

    @Override
    public void enforceCallingOrSelfPermission(String permission, String message) {
        hostContext.enforceCallingOrSelfPermission(permission, message);
    }

    @Override
    public void grantUriPermission(String toPackage, Uri uri, int modeFlags) {
        hostContext.grantUriPermission(toPackage, uri, modeFlags);
    }

    @Override
    public void revokeUriPermission(Uri uri, int modeFlags) {
        hostContext.revokeUriPermission(uri, modeFlags);
    }

    @Override
    public int checkUriPermission(Uri uri, int pid, int uid, int modeFlags) {
        return hostContext.checkUriPermission(uri, pid, uid, modeFlags);
    }

    @Override
    public int checkCallingUriPermission(Uri uri, int modeFlags) {
        return hostContext.checkCallingUriPermission(uri, modeFlags);
    }

    @Override
    public int checkCallingOrSelfUriPermission(Uri uri, int modeFlags) {
        return hostContext.checkCallingOrSelfUriPermission(uri, modeFlags);
    }

    @Override
    public int checkUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags) {
        return hostContext.checkUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags);
    }

    @Override
    public void enforceUriPermission(Uri uri, int pid, int uid, int modeFlags, String message) {
        hostContext.enforceUriPermission(uri, pid, uid, modeFlags, message);
    }

    @Override
    public void enforceCallingUriPermission(Uri uri, int modeFlags, String message) {
        hostContext.enforceCallingUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceCallingOrSelfUriPermission(Uri uri, int modeFlags, String message) {
        hostContext.enforceCallingOrSelfUriPermission(uri, modeFlags, message);
    }

    @Override
    public void enforceUriPermission(Uri uri, String readPermission, String writePermission, int pid, int uid, int modeFlags, String message) {
        hostContext.enforceUriPermission(uri, readPermission, writePermission, pid, uid, modeFlags, message);
    }
}
