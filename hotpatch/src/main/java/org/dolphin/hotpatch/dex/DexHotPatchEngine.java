package org.dolphin.hotpatch.dex;

import android.app.Application;
import android.os.Build;

import org.dolphin.hotpatch.AndroidMainThreadScheduler;
import org.dolphin.http.HttpRequest;
import org.dolphin.job.Job;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.HttpJobs;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.SwallowExceptionOperator;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.util.Log;
import org.dolphin.lib.util.IOUtil;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;


/**
 * 所有的文件包含xxxxxxxxxxx.dex，所有的配置在global.config.json <br>
 * 所有的分为冷部署和热部署，冷部署时启动的时候才能部署，即启动时同步加载；<br>热部署是可以在运行中加载的，即异步加载
 * 加载流程如下：<br>
 * 1. 从本地读取接口global.config.json, 决定需要冷部署的dex；如果没有config文件，则等待更新信息在进行加载,跳转到步骤3；<br>
 * 2. 根据步骤1的配置文件加载type为0的dex；<br>
 * 3. 从server拉取更新后的global.config.json, 比对后，如果没有更新，则加载type为1的热部署，加载可用的的热部署dex，则加载；<br>
 * 4. 存在有效的未下载的dex，则从服务器拉取后在加载, 对比以前的，只做增量更新，对于已经加载的并且此次无效的，不需要做其他处理。<br>
 * <p/>
 * <b>对于冷部署，启动的是上次的，本次的需要在下次更新才能用，热部署可以是延时加载的。</b>
 *
 * @param <T> the sub class of application
 */
public class DexHotPatchEngine<T extends Application> {
    public static final String UPDATE_URL = "http://172.18.16.47:12345/update";
    public static final String DIRECTORY_NAME = "dex_private_dir";
    public static final String GLOBAL_CONFIG_NAME = "global.config.json";
    public static final String TAG = "DexHotPatchEngine";
    public static final Scheduler sUpdateScheduler = Schedulers.newThread();
    private static DexHotPatchEngine sInstance = null;

    public synchronized static <T extends Application> DexHotPatchEngine instance(T applicationContext) {
        if (null == sInstance) {
            sInstance = new DexHotPatchEngine(applicationContext);
        }
        return sInstance;
    }

    private final WeakReference<T> applicationRef;
    private final File privateDexDirectory;
    private final File dexOptemizeDir;
    // 已经加载的identify - DexNameStruct的映射
    private final HashMap<String, DexLocalStruct> loadedDexMap = new HashMap<String, DexLocalStruct>();
    private boolean notSupport = false;
    private DexUpdateBean dexUpdateBean;

    private DexHotPatchEngine(T applicationContext) {
        this.applicationRef = new WeakReference<T>(applicationContext);
        privateDexDirectory = new File(applicationContext.getFilesDir(), DIRECTORY_NAME);
        dexOptemizeDir = new File(privateDexDirectory, "dex");
        dexUpdateBean = DexUpdateBean.readFromFile(new File(privateDexDirectory, GLOBAL_CONFIG_NAME));
        init();
    }

    private void init() {
        if (!privateDexDirectory.isDirectory()) {
            Log.w(TAG, "File " + privateDexDirectory.getAbsolutePath() + " is not directory, not support load extra dex!");
            notSupport = true;
        }

        if (!privateDexDirectory.exists()) {
            notSupport = !privateDexDirectory.mkdir();
            if (notSupport) {
                Log.w(TAG, "File " + privateDexDirectory.getAbsolutePath() + " create failed!");
                return;
            }
        }

        if (!dexOptemizeDir.exists()) {
            notSupport = !dexOptemizeDir.mkdir();
            if (notSupport) {
                Log.w(TAG, "File " + dexOptemizeDir.getAbsolutePath() + " create failed!");
                return;
            }
        }
    }

    // 返回当前保存的application
    protected T getApplication() {
        return applicationRef.get();
    }

    // 只会在当前的目录下的所有file
    private synchronized List<String> scanDirector(File privateDexDirectory, String... excludeFileName) {
        if (notSupport) return null;

        final List<String> fileList = new ArrayList<String>();
        final Set<String> exclude = new HashSet<String>();
        if (excludeFileName != null) {
            exclude.addAll(Arrays.asList(excludeFileName));
        }
        String[] fileNameList = privateDexDirectory.list();
        if (null != fileNameList && fileNameList.length > 0) {
            for (String name : fileNameList) {
                if (exclude.contains(name)) continue;
                fileList.add(name);
            }
        }

        return fileList;
    }

    /**
     * 加载指定的dex list
     *
     * @param expectedLoadList   希望能马上加载的dex list
     * @param nextWaitingLoadDex 作为输出，输出现在不能加载的dex
     * @param justLoadColdDex    是否只加载冷部署的dex
     * @param needCheckSign      是否需要校验文件签名
     */
    private synchronized void load(final List<DexLocalStruct> expectedLoadList, final List<DexLocalStruct> nextWaitingLoadDex,
                                   final boolean justLoadColdDex, final boolean needCheckSign) {
        Log.d(TAG, "onLoading, just Load Cold Dex? " + justLoadColdDex);
        Log.d(TAG, "onLoading, Need check sign? " + needCheckSign);
        if (null == expectedLoadList || expectedLoadList.isEmpty()) {
            Log.i(TAG, "onLoading, No any dex expect to load! Return!");
            return;
        }

        if (notSupport || null == getApplication()) {
            Log.i(TAG, "onLoading, Not support load extra Dex or current application has terminal! Return!");
            return;
        }

        ArrayList<File> loadingDexFileList = new ArrayList<File>();
        Map<String, DexLocalStruct> loadingDexMap = new LinkedHashMap<String, DexLocalStruct>();
        for (DexLocalStruct struct : expectedLoadList) {
            Log.d(TAG, "onLoading, try loading dex " + struct.toString() + ", type " + struct.type);
            if (this.loadedDexMap.containsKey(struct.fileName)) {
                Log.d(TAG, "onLoading, " + struct.toString() + " has been loaded! ignore this dex!");
                continue;
            }

            if (justLoadColdDex && struct.type != 0) { // 不需要加载hot dex
                nextWaitingLoadDex.add(struct);
                continue;
            }

            File file = new File(privateDexDirectory, struct.fileName);
            if (file.exists() && file.canRead() && !file.isDirectory() && file.canWrite()
                    && (!needCheckSign || DexHotPatchJobHelper.isSignMatch(struct, file))) {
                // 如果需要，会比较签名
                Log.d(TAG, "onLoading, Loading dex " + struct.toString() + " into waiting queue!");
                loadingDexFileList.add(file);
                loadingDexMap.put(struct.fileName, struct);
            } else {
                Log.d(TAG, "onLoading, Because of file or sign unMatch, currently will not loaded, loading delay!");
                nextWaitingLoadDex.add(struct);
            }
        }

        if (loadingDexFileList.isEmpty()) {
            Log.d(TAG, "onLoading, Currently do not need load any dex, Return!");
            return;
        }

        Log.d(TAG, "Loading, try load " + getPrintableLog(loadingDexMap.values()));
        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                DexHotPatchInstallHelper.V19.install(getApplication().getClassLoader(), loadingDexFileList, dexOptemizeDir);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                DexHotPatchInstallHelper.V14.install(getApplication().getClassLoader(), loadingDexFileList, dexOptemizeDir);
            } else {
                // TODO
            }
            this.loadedDexMap.putAll(loadingDexMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            nextWaitingLoadDex.addAll(loadingDexMap.values());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            nextWaitingLoadDex.addAll(loadingDexMap.values());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            nextWaitingLoadDex.addAll(loadingDexMap.values());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            nextWaitingLoadDex.addAll(loadingDexMap.values());
        }
    }

    public static String getPrintableLog(Collection<DexLocalStruct> nextWaitingLoadDex) {
        if (null == nextWaitingLoadDex || nextWaitingLoadDex.isEmpty()) return "[N/A]";
        StringBuilder sb = new StringBuilder();
        sb.append('[');
        for (DexLocalStruct dex : nextWaitingLoadDex) {
            if (sb.length() > 1) sb.append(',');
            sb.append(dex.fileName);
        }
        sb.append(']');
        return sb.toString();
    }

    public synchronized void attachToApplication() {
        if (notSupport) {
            Log.e(TAG, "attachToApplication, Not support load extra dex! Abort!");
            return;
        }
        final DexUpdateBean prevDexUpdateBean = this.dexUpdateBean;
        // 存在两种情况：1，需要马上加载的cold dex本地不存在；2， 不需要马上加在的hot dex
        final List<DexLocalStruct> nextWaitingLoadDex = new ArrayList<DexLocalStruct>();
        load(null == prevDexUpdateBean ? null : Arrays.asList(prevDexUpdateBean.dexConfigBeans), nextWaitingLoadDex, true, true); // 得到所有需要二次加载的dex
        Log.d(TAG, "attachToApplication, All the loadable dex has loaded, now load the missing dex list! " + getPrintableLog(nextWaitingLoadDex));

        HashMap<String, String> params = new HashMap<String, String>();
        if (null != prevDexUpdateBean) {
            params.put("version", prevDexUpdateBean.version);
        }
        HttpRequest request = HttpJobs.createGetRequest(UPDATE_URL, params); // fetch global.config.json文件
        Job updateJob = new Job(request);
        updateJob
                .then(new SwallowExceptionOperator(new HttpPerformOperator(), null))
                .then(new SwallowExceptionOperator(new HttpResponseToBytes(), null))
                .then(new Operator<byte[], List<DexLocalStruct>>() { // 返回需要继续加载的dex list
                    @Override
                    public List<DexLocalStruct> operate(byte[] input) throws Throwable {
                        final List<DexLocalStruct> nextLoadingDex = new ArrayList<DexLocalStruct>(nextWaitingLoadDex); // 需要后继加载的dex
                        if (input == null || input.length <= 0) {
                            Log.d(TAG, "Fetch update information from server failed! Load dex from current existent config.json!");
                            return nextLoadingDex;
                        }

                        if (input[0] == 0) { // 表示没有更新，当前的version是最新的, 需要下载需要的dex到指定的目录中去
                            Log.d(TAG, "Fetch update information from server, No need to update global.config.json!");
                        } else {
                            Log.d(TAG, "Fetch update information from server, Need to update global.config.json!");
                            File configFile = new File(privateDexDirectory, GLOBAL_CONFIG_NAME);
                            IOUtil.write(configFile, input, 1, input.length - 1);
                            DexUpdateBean newUpdateBean = DexUpdateBean.readFromBytes(input, 1, input.length - 1);
                            DexHotPatchEngine.this.dexUpdateBean = newUpdateBean;
                            if (null == newUpdateBean) {
                                Log.e(TAG, "Cannot get global.config.json from server, then do it by before config.json!Break Up!");
                            } else {
                                nextLoadingDex.clear();
                                if (null == newUpdateBean || newUpdateBean.dexConfigBeans == null || newUpdateBean.dexConfigBeans.length <= 0) {
                                    Log.i(TAG, "Fetch update information from server, No any dex need to load! Do nothing!");
                                } else {
                                    for (DexLocalStruct struct : newUpdateBean.dexConfigBeans) {
                                        Log.d(TAG, "Fetch update information from server, Need load new config struct -> " + struct.toString());
                                        if (loadedDexMap.containsKey(struct.fileName) || nextLoadingDex.contains(struct)) {
                                            Log.d(TAG, "Fetched new config -> " + struct.toString() + " has loaded or will load duplicate! ignore this dex!");
                                            continue;
                                        }
                                        nextLoadingDex.add(struct);
                                    }
                                }
                            }
                        }

                        return nextLoadingDex;
                    }
                })
                .then(new Operator<List<DexLocalStruct>, List<DexLocalStruct>>() { // 现在需要的dex，并且返回有效的dex, TODO:是否可以返回所有的dex list？
                    @Override
                    public List<DexLocalStruct> operate(List<DexLocalStruct> waitingLoadDexList) throws Throwable {
                        if (waitingLoadDexList == null || waitingLoadDexList.isEmpty())
                            return waitingLoadDexList;
                        List<DexLocalStruct> failedList = new ArrayList<DexLocalStruct>();
                        DexHotPatchJobHelper.downLoadDexListIfNeed(privateDexDirectory, waitingLoadDexList, failedList);
                        for (DexLocalStruct struct : failedList) {
                            Log.e(TAG, "Download " + struct.toString() + " Failed!");
                            waitingLoadDexList.remove(struct);
                        }

                        return waitingLoadDexList;
                    }
                })
                .observer(new Observer.SimpleObserver<Void, List<DexLocalStruct>>() {
                    @Override
                    public void onCompleted(Job job, List<DexLocalStruct> nextLoadingDex) {
                        if (null == nextLoadingDex || nextLoadingDex.isEmpty()) {
                            Log.d(TAG, "onCompleted, No need to load any other dex!Complete");
                            return;
                        }
                        load(nextLoadingDex, nextLoadingDex, false, false); // 加载所有需要的dex，包括新的和旧的，不需要验证签名
                        Job clearJob = new Job(null); // 删除
                        clearJob.then(new Operator() { // 删除没有用的文件
                            @Override
                            public Object operate(Object input) throws Throwable {
                                List<String> files = scanDirector(privateDexDirectory, GLOBAL_CONFIG_NAME);
                                for (String fileName : files) {
                                    if (loadedDexMap.containsKey(fileName)) continue;
                                    deleteFile(fileName);
                                }
                                return input;
                            }
                        }).workOn(sUpdateScheduler).work();
                    }
                })
                .workOn(sUpdateScheduler)
                .observerOn(AndroidMainThreadScheduler.INSTANCE)
                .work();
    }

    public synchronized List<DexLocalStruct> getLoadedDex() {
        return new ArrayList<DexLocalStruct>(this.loadedDexMap.values());
    }

    public synchronized void deleteFile(String fileName) {
        File file = new File(privateDexDirectory, fileName);
        if (!file.exists() || !file.isFile()) {
            Log.i(TAG, "File " + file.getAbsolutePath() + " is not a normal file, do not delete it!");
            return;
        }
        Log.i(TAG, "Delete File " + file.getAbsolutePath());
        IOUtil.safeDeleteIfExists(file);
    }
}
