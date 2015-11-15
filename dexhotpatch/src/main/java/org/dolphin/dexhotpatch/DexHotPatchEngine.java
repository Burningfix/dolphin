package org.dolphin.dexhotpatch;

import android.app.Application;
import android.os.Build;

import org.dolphin.http.HttpGetLoader;
import org.dolphin.http.HttpLoader;
import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.ValueReference;

import java.io.File;
import java.lang.ref.Reference;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
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
    public static final String UPDATE_URL = "http://www.baidu.com";
    public static final String DIRECTORY_NAME = "dex_private_dir";
    public static final String GLOBAL_CONFIG_NAME = "global.config.json";
    public static final String TAG = "DexHotPatchEngine";
    public static final Scheduler sUpdateSchduler = Schedulers.newThread();
    private static DexHotPatchEngine sInstance = null;

    public synchronized static <T extends Application> DexHotPatchEngine instance(T applicationContext) {
        if (null == sInstance) {
            sInstance = new DexHotPatchEngine(applicationContext);
        }
        return sInstance;
    }

    private final WeakReference<T> applicationRef;
    private final File privateDexDirectory;
    // 已经加载的identify - DexNameStruct的映射
    private final HashMap<String, DexLocalStruct> loadedDexMap = new HashMap<String, DexLocalStruct>();
    private boolean notSupport = false;
    private DexUpdateBean dexUpdateBean;
    private Job updateJob;

    private DexHotPatchEngine(T applicationContext) {
        this.applicationRef = new WeakReference<T>(applicationContext);
        privateDexDirectory = new File(applicationContext.getFilesDir(), DIRECTORY_NAME);
        dexUpdateBean = DexUpdateBean.readFromFile(new File(privateDexDirectory, GLOBAL_CONFIG_NAME));

        attachToApplication();
    }

    // 返回当前保存的application
    protected T getApplication() {
        return applicationRef.get();
    }

    // 只会在当前的目录下的所有file
    private synchronized List<String> scanDirector(File privateDexDirectory, String... excludeFileName) {
        if (!privateDexDirectory.isDirectory()) {
            Log.w(TAG, "File " + privateDexDirectory.getAbsolutePath() + " is not directory, not support load extra dex!");
            notSupport = true;
        }

        if (!privateDexDirectory.exists()) {
            notSupport = privateDexDirectory.mkdir();
            if (notSupport) {
                Log.w(TAG, "File " + privateDexDirectory.getAbsolutePath() + " create failed!");
            }
        }
        if (notSupport) return null;

        final List<String> fileList = new ArrayList<String>();
        final Set<String> exclude = new HashSet<String>();
        if (excludeFileName != null) {
            exclude.addAll(Arrays.asList(excludeFileName));
        }
        String[] fileNameList = privateDexDirectory.list();
        if (null != fileNameList && fileNameList.length <= 0) {
            for (String name : fileNameList) {
                if (exclude.contains(name)) continue;
                fileList.add(name);
            }
        }

        return fileList;
    }

    private synchronized void load(DexUpdateBean configBean, List<DexLocalStruct> nextLoadDex, final boolean justLoadColdDex, final boolean needCheckSign) {
        if (!notSupport || null == getApplication()) {
            Log.d(TAG, "Not support loadDex!");
            return;
        }

        if (null == configBean || configBean.dexConfigBeans == null || configBean.dexConfigBeans.length <= 0) {
            Log.i(TAG, "No any dex need to load!");
            return;
        }
        Log.d(TAG, "load justLoadColdDex? " + justLoadColdDex);
        ArrayList<File> loadingDexFileList = new ArrayList<File>();
        HashMap<String, DexLocalStruct> loadingDexMap = new HashMap<String, DexLocalStruct>();
        for (DexLocalStruct struct : configBean.dexConfigBeans) {
            Log.d(TAG, "Found dex struct " + struct.fileName + ", type " + struct.type);
            if (this.loadedDexMap.containsKey(struct.fileName)) {
                Log.d(TAG, struct.fileName + " has loaded! ignore this dex!");
                continue;
            }

            if (justLoadColdDex && struct.type != 0) {
                nextLoadDex.add(struct);
                continue;
            }

            File file = new File(privateDexDirectory, struct.fileName);
            if (file.exists() && file.canRead() && !file.isDirectory() && file.canWrite()
                    && (!needCheckSign || DexHotPatchJobHelper.isSignMatch(struct, file))) {
                // 如果需要，会比较签名
                Log.d(TAG, "Loading dex " + struct.toString());
                loadingDexFileList.add(file);
                loadingDexMap.put(struct.fileName, struct);
            } else {
                Log.d(TAG, "Because file or sign problem, currently will not loaded, loading delay!");
                nextLoadDex.add(struct);
            }
        }

        if (loadingDexFileList.isEmpty()) {
            Log.d(TAG, "Can not load any dex, Return!");
            return;
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                DexHotPatchInstallHelper.V19.install(getApplication().getClassLoader(), loadingDexFileList, privateDexDirectory);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                DexHotPatchInstallHelper.V14.install(getApplication().getClassLoader(), loadingDexFileList, privateDexDirectory);
            } else {
                // TODO
            }
            this.loadedDexMap.putAll(loadingDexMap);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            nextLoadDex.addAll(loadingDexMap.values());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            nextLoadDex.addAll(loadingDexMap.values());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            nextLoadDex.addAll(loadingDexMap.values());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            nextLoadDex.addAll(loadingDexMap.values());
        }
    }

    public synchronized void attachToApplication() {
        final DexUpdateBean prevDexUpdateBean = this.dexUpdateBean;
        HashMap<String, String> params = new HashMap<String, String>();
        if (null != prevDexUpdateBean) {
            params.put("version", prevDexUpdateBean.version);
        }
        final List<DexLocalStruct> nextLoadedDex = new ArrayList<DexLocalStruct>();
        load(prevDexUpdateBean, nextLoadedDex, true, true); // 加载所有可用的冷部署类型的dex

        HttpRequest request = HttpJobs.createGetRequest(UPDATE_URL, params); // fetch global.config.json文件
        Job updateJob = new Job(request);
        updateJob
                .append(new Operator() { // delete useless file
                    @Override
                    public Object operate(Object input) throws Throwable {
                        List<String> files = scanDirector(privateDexDirectory, GLOBAL_CONFIG_NAME);
                        for (String fileName : files) {
                            if (loadedDexMap.containsKey(fileName)) continue;
                            deleteFile(fileName);
                        }
                        return input;
                    }
                })
                .append(new HttpPerformOperator())
                .append(new HttpResponseToBytes())
                .append(new Operator<byte[], List<DexLocalStruct>>() { // 返回需要继续加载的dex list
                    @Override
                    public List<DexLocalStruct> operate(byte[] input) throws Throwable {
                        List<DexLocalStruct> nextLoadingDex = new ArrayList<DexLocalStruct>();
                        byte lastByte = input[0]; // 返回的最前面的就是标识位
                        if (lastByte == 0) { // 表示没有更新，当前的version是最新的, 需要下载需要的dex到指定的目录中去
                            nextLoadingDex.addAll(nextLoadedDex);
                        } else {
                            File configFile = new File(privateDexDirectory, GLOBAL_CONFIG_NAME);
                            IOUtil.write(configFile, input, 1, input.length - 1);
                            DexUpdateBean newUpdateBean = DexUpdateBean.readFromBytes(input, 1, input.length - 1);
                            DexHotPatchEngine.this.dexUpdateBean = newUpdateBean;
                            if (null == newUpdateBean) {
                                Log.e(TAG, "Cannot get global.config.json from server, then do nothing!Break Up!");
                            } else {
                                if (null == newUpdateBean || newUpdateBean.dexConfigBeans == null || newUpdateBean.dexConfigBeans.length <= 0) {
                                    Log.i(TAG, "No any dex need to load!");
                                } else {
                                    for (DexLocalStruct struct : newUpdateBean.dexConfigBeans) {
                                        Log.d(TAG, "Fetched new config struct -> " + struct.toString());
                                        if (loadedDexMap.containsKey(struct.fileName)) {
                                            Log.d(TAG, "Fetched new config -> " + struct.fileName + " has loaded! ignore this dex!");
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
                .append(new Operator<List<DexLocalStruct>, List<DexLocalStruct>>() { // 现在需要的dex，并且返回有效的dex, TODO:是否可以返回所有的dex list？
                    @Override
                    public List<DexLocalStruct> operate(List<DexLocalStruct> input) throws Throwable {
                        if (input == null || input.isEmpty()) return input;
                        List<DexLocalStruct> failedList = new ArrayList<DexLocalStruct>();
                        DexHotPatchJobHelper.downLoadDexList(privateDexDirectory, input, failedList);
                        for (DexLocalStruct struct : failedList) {
                            Log.e(TAG, "Download " + struct.toString() + " Failed!");
                            input.remove(struct);// 删除无效的，下载失败的
                        }

                        return input;
                    }
                })
                .observer(new Observer.SimpleObserver<Void, List<DexLocalStruct>>() {
                    @Override
                    public void onCompleted(Job job, List<DexLocalStruct> nextLoadingDex) {
                        load(dexUpdateBean, nextLoadingDex, false, false); // 加载所有需要的dex，包括新的和旧的，不需要验证签名
                    }
                })
                .workOn(sUpdateSchduler)
                .observerOn(AndroidMainThreadScheduler.INSTANCE)
                .work();
    }

    public synchronized List<DexLocalStruct> getLoadedDex() {
        return new ArrayList<DexLocalStruct>(this.loadedDexMap.values());
    }

    public synchronized void deleteFile(String fileName) {
        IOUtil.safeDeleteIfExists(new File(privateDexDirectory, fileName));
    }
}
