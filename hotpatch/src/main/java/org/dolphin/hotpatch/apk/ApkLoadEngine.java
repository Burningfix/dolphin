package org.dolphin.hotpatch.apk;

import android.content.Context;
import android.os.SystemClock;
import android.renderscript.ScriptGroup;
import android.util.Log;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.http.HttpUrlLoader;
import org.dolphin.job.Job;
import org.dolphin.job.Observer;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.concurrent.locks.ReentrantReadWriteLock;

/**
 * Created by hanyanan on 2015/11/20.
 * <p/>
 * 所有的apk都必须存在org.dolphin.plugin.apk.ApkPluginConfig.java作为该apk的配置选项<br>
 */
public class ApkLoadEngine {
    public static final String UPDATE_CONFIG_URL = "http://172.18.16.37:23456/update";
    public static final String URL = "http://172.18.16.37:23456/apk";
    public static final String TAG = "ApkLoadEngine";
    public static final String GLOBAL_CONFIG_NAME = "global_apk_config.json";
    public static final String APK_SUFFIX = ".apk";
    private static ApkLoadEngine sInstance = null;

    public synchronized static ApkLoadEngine instance(File privateStorageDirectory, File optimizedDirectory) {
        if (null == sInstance) {
            sInstance = new ApkLoadEngine(privateStorageDirectory, optimizedDirectory);
        }
        return sInstance;
    }

    private final ReentrantReadWriteLock readWriteLock = new ReentrantReadWriteLock();
    private final ReentrantReadWriteLock.ReadLock readerLock = readWriteLock.readLock();
    private final ReentrantReadWriteLock.WriteLock writerLock = readWriteLock.writeLock();
    private final Map<String, ApkPlugin> loadedApk = new LinkedHashMap<String, ApkPlugin>();
    private final File optimizedDirectory;
    private final File privateStorageDirectory;
    private GlobalConfigBean globalConfigBean = null;
    private final Map<String, GlobalConfigBean.ApkPluginConfig> configMap = new LinkedHashMap<String, GlobalConfigBean.ApkPluginConfig>();
    private Job previousUpdateJob = null;

    private ApkLoadEngine(File privateStorageDirectory, File optimizedDirectory) {
        this.optimizedDirectory = optimizedDirectory;
        this.privateStorageDirectory = privateStorageDirectory;

        update();
    }

    public synchronized void update() {
        if (null != previousUpdateJob) previousUpdateJob.abort();
        File beanFile = new File(privateStorageDirectory, GLOBAL_CONFIG_NAME);
        Job job = ApkLoadJobHelper.createUpdateJob(UPDATE_CONFIG_URL, beanFile, GlobalConfigBean.class);
        job.observer(new Observer.SimpleObserver<Object, GlobalConfigBean>() {
            @Override
            public void onCompleted(Job job, GlobalConfigBean result) {
                previousUpdateJob = null;
                syncGlobalConfig(result);
                synchronization();
            }

            @Override
            public void onFailed(Job job, Throwable error) {
                super.onFailed(job, error);
                previousUpdateJob = null;
                syncGlobalConfig(null);
                synchronization();
            }
        });
        job.work();
        previousUpdateJob = job;
    }

    private synchronized void syncGlobalConfig(GlobalConfigBean result) {
        writerLock.lock();
        try {
            this.globalConfigBean = result;
            this.configMap.clear();
            if (null == result) {
                Log.i(TAG, "syncGlobalConfig, Not support load apk!");
            }
            GlobalConfigBean.ApkPluginConfig[] configs = result.apkPluginConfigs;
            if (configs == null || configs.length <= 0) {
                Log.i(TAG, "[Exit|syncGlobalConfig], there is no any apk!");
                return;
            }
            for (GlobalConfigBean.ApkPluginConfig apkPluginConfig : configs) {
                configMap.put(apkPluginConfig.id, apkPluginConfig);
            }
        } finally {
            writerLock.unlock();
        }
    }


    // 同步所有的apk,需要自动同步非延时的apk
    private synchronized void synchronization() {
        Log.i(TAG, "Start synchronization!");
        readerLock.lock();
        try {
            for (GlobalConfigBean.ApkPluginConfig apkPluginConfig : configMap.values()) {
                if (apkPluginConfig.delayLoad == 0) {
                    asyncLoadApk(apkPluginConfig.id, new ApkLoadObserverWrapper(null));
                }
            }
        } finally {
            readerLock.unlock();
        }
    }

    public static boolean signMatch(File file, String sign) {
        return true;
    }

    public static ApkPlugin loadApk(String id, File apkFile, String optimizedDir,
                                    GlobalConfigBean.ApkPluginConfig config, Context context) throws Throwable {
        Log.d(TAG, "[Start|loadApk] Load Apk " + id);
        // 1. 尝试从disk中读取
        if (null != apkFile && apkFile.canRead() && apkFile.isFile() && signMatch(apkFile, config.sign)) {
            Log.d(TAG, "[Loading|loadApk] " + id + ", Try loading from disk!");
            ApkPlugin apkPlugin = loadApkFromDisk(apkFile, optimizedDir, context);
            if (null != apkPlugin) {
                Log.d(TAG, "[Exit|loadApk|Success] " + id + ", Loading Success!");
                return apkPlugin;
            }
            Log.d(TAG, "[Loading|loadApk] " + id + ", Loading from disk Failed! Load from server!");
        }
        // 2. 从server下载当前的apk
        HttpRequest httpRequest = parseApkRequest(id, null);
        HttpResponse response = null;
        InputStream inputStream = null;
        OutputStream outputStream = null;
        try {
            Log.d(TAG, "[Loading|loadApk|DownLoading] " + id + ", Download from server!");
            response = HttpUrlLoader.getHttpResponse(httpRequest);
            if (null == response) {
                return null;
            }
            inputStream = response.body();
            if (inputStream == null) {
                return null;
            }
            outputStream = new FileOutputStream(apkFile);
            IOUtil.copy(inputStream, outputStream);
            Log.d(TAG, "[Loading|loadApk|DownLoaded] " + id + ", Download from server Finished!");
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw throwable;
        } finally {
            IOUtil.closeQuietly(httpRequest);
            IOUtil.closeQuietly(inputStream);
            IOUtil.closeQuietly(outputStream);
            IOUtil.closeQuietly(response);
        }
        // 3. 从本地存储中尝试加载
        Log.d(TAG, "[Loading|loadApk] " + id + ", Loading from disk file which download from server just now!");
        ApkPlugin apkPlugin = loadApkFromDisk(apkFile, optimizedDir, context);
        Log.d(TAG, "[Exit|loadApk] " + id + ", Result is " + apkPlugin);
        return apkPlugin;
    }

    public synchronized void asyncLoadApk(final String id, final ApkLoadObserver observer) {
        final File apkFile = new File(privateStorageDirectory, id + APK_SUFFIX);
        final Context context = null;
        GlobalConfigBean.ApkPluginConfig config = null;
        readerLock.lock();
        try {
            config = configMap.get(id);
        } finally {
            readerLock.unlock();
        }

        if (null == config) {
            Log.e(TAG, "Cannot load apk " + id + " as Can not found from global bean!");
            observer.onApkLoadFailed(id, new IllegalAccessException("Apk Id" + id + " is Illegal!"));
            return;
        }

        Job job = new Job(id);


        return;
    }

    /**
     * 尝试从现在的类中加载id为指定的apk，如果当前缓存存在指定的apk，则直接返回指定的文件，否则，会先从server
     * 现在指定的apk，然后在进行加载
     *
     * @param id
     * @param observer
     * @return
     */
    public synchronized ApkPlugin tryLoadApk(String id, ApkLoadObserver observer) {
        readerLock.lock();
        try {
            ApkPlugin apkPlugin = loadedApk.get(id);
            if (null != apkPlugin) return apkPlugin;
        } finally {
            readerLock.unlock();
        }

        asyncLoadApk(id, observer);
        return null;
    }


    private class ApkLoadObserverWrapper implements ApkLoadObserver {
        private final ApkLoadObserver delegateApkLoadObserver;

        private ApkLoadObserverWrapper(ApkLoadObserver delegateApkLoadObserver) {
            this.delegateApkLoadObserver = delegateApkLoadObserver;
        }

        @Override
        public void onApkLoaded(ApkPlugin apkPlugin) {
            writerLock.lock();
            try {
                loadedApk.put(apkPlugin.getId(), apkPlugin);
            } finally {
                writerLock.unlock();
            }

            if (null != delegateApkLoadObserver) {
                delegateApkLoadObserver.onApkLoaded(apkPlugin);
            }
        }

        @Override
        public void onApkLoadFailed(String id, Throwable throwable) {
            Log.e(TAG, "Failed Load Apk " + id + "[" + throwable.getMessage() + "]");
            if (null != delegateApkLoadObserver) {
                delegateApkLoadObserver.onApkLoadFailed(id, throwable);
            }
        }
    }

    private static ApkPlugin loadApkFromDisk(File localApkPath, String optimizedDirectory, Context context) {
        if (!localApkPath.isFile() || !localApkPath.exists() || !localApkPath.canRead())
            throw new RuntimeException("");
        long t1 = SystemClock.elapsedRealtime();
        HotPlugInClassLoader classLoader = new HotPlugInClassLoader(localApkPath.getPath(), optimizedDirectory, null, context.getClassLoader());
        try {
            Class<ApkPluginInterface> clazz = (Class<ApkPluginInterface>) classLoader.loadClass("org.dolphin.plugin.apk.ApkPluginConfig");
            ApkPluginInterface apkPluginInterface = clazz.newInstance();
            long loadedTimeStamp = SystemClock.elapsedRealtime();
            ApkPlugin.Builder builder = new ApkPlugin.Builder();
            builder.setClassLoader(classLoader);
            builder.setDescriptor(apkPluginInterface.descriptor);
            builder.setExtensionConfig(apkPluginInterface.extensionConfig);
            builder.setId(apkPluginInterface.id);
            builder.setName(apkPluginInterface.name);
            builder.setPath(localApkPath.getPath());
            builder.setRiseCost(loadedTimeStamp - t1);
            builder.setRiseTime(t1);
            builder.setSize(localApkPath.length());
            builder.setSign("AAAAAAAA"); // TODO
            return builder.build();
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (InstantiationException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static HttpRequest parseApkRequest(String id, Map<String, String> params) {
        HttpRequest request = new HttpRequest(URL);
        if (null == params) {
            params.put("id", id);
        }
        request.params(params);
        return request;
    }
}
