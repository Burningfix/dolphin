package org.dolphin.dexhotpatch;

import android.app.Application;
import android.os.Build;

import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Observer;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

///**
// * Created by hanyanan on 2015/11/11.
// * <p/>
// * <p/>
// * 一共有三种dex<br>
// * ---------------------------------------------------------------------------------------------------------------------<br>
// * 0_0_xxxxxxxxx_122222222.dex                      用于取代当前apk中的class，仅用于打补丁，在应用加载时同步执行<br>
// * 0_0_xxxxxxxxx_122222222.config.json              用于0_xxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息<br>
// * 1_2_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用加载时同步执行<br>
// * 1_2_xxxxxxxxx_122222222.config.json              用于1_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层<br>
// * 2_5_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用启动后异步加载<br>
// * 2_5_xxxxxxxxx_122222222.config.json              用于12_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层<br>
// * 第一位0表示取代当前的class，用于线上修复bug；<br>
// * 1代表添加功能需要在app启动时同步进行，用于高优先级页面；<br>
// * 2代表添加功能，需要在app启动后，后台线程进行，由于低优先级页面, 会等到与server通讯完成在进行比对进行加载.<br>
// * 中间数字表示优先级，大小越低，优先级越高，越先加载，最小为0, 决定加载顺序<br>
// * 第三个单元表示是identify，用于dex的key，各个只能有一个<br>
// * <b>第四个单元表示下载的日期，越新的会清除旧的,这个时间是server下发的时间，而非本地时间</b><br>
// * <b>任何合法的都必选且只能包含以上四个部分</b><br>
// * <b>所有可能出现的a-z全小写</b><br>
// * <b>当只存在dex文件，而没有对应的json文件或者json文件无效时，需要删除掉对应的dex和config.json</b><br>
// *<br>
// * 加载流程如下：<br>
// * 1. 从本地读取接口global.config.json, 决定需要加载的dex，如果没有此文件，则等待更新信息在进行加载,跳转到步骤3；<br>
// * 2. 根据步骤1的配置文件加载type为0，1的dex；<br>
// * 3. 从server拉取更新后的global.config.json, 并写入到磁盘，如果有未加载的有效的本地的dex，则加载；<br>
// * 4. 存在有效的未下载的dex，则从服务器拉取后在加载, 对比以前的，只做增量更新，对于已经加载的并且此次无效的，不需要做其他处理。<br>
// */

/**
 * 所有的文件包含xxxxxxxxxxx.dex，所有的配置在global.config.json <br>
 * 加载流程如下：<br>
 * 1. 从本地读取接口global.config.json, 决定需要加载的dex，如果没有此文件，则等待更新信息在进行加载,跳转到步骤3；<br>
 * 2. 根据步骤1的配置文件加载type为0，1的dex；<br>
 * 3. 从server拉取更新后的global.config.json, 并写入到磁盘，如果有未加载的有效的本地的dex，则加载；<br>
 * 4. 存在有效的未下载的dex，则从服务器拉取后在加载, 对比以前的，只做增量更新，对于已经加载的并且此次无效的，不需要做其他处理。<br>
 *
 * @param <T>
 */
public class DexHotPatchEngine<T extends Application> {
    public static final String UPDATE_URL = "http://www.baidu.com";
    public static final String DIRECTORY_NAME = "dex_private_dir";
    public static final String GLOBAL_CONFIG_NAME = "global.config.json";
    public static final String TAG = "DexHotPatchEngine";
    private static DexHotPatchEngine sInstance = null;

    public synchronized static <T extends Application> DexHotPatchEngine instance(T applicationContext, DexLoadObserver observer) {
        if (null == sInstance) {
            sInstance = new DexHotPatchEngine(applicationContext, observer);
        }
        return sInstance;
    }

    private final WeakReference<T> applicationRef;
    private final DexLoadObserver observer;
    private final File privateDexDirectory;
    // 已经加载的identify - DexNameStruct的映射
    private final HashMap<String, DexLocalStruct> loadedDexMap = new HashMap<String, DexLocalStruct>();
    private boolean notSupport = false;
    private DexUpdateBean dexUpdateBean;

    private DexHotPatchEngine(T applicationContext, DexLoadObserver observer) {
        this.applicationRef = new WeakReference<T>(applicationContext);
        this.observer = observer;
        privateDexDirectory = new File(applicationContext.getFilesDir(), DIRECTORY_NAME);
        dexUpdateBean = DexUpdateBean.readFromFile(new File(privateDexDirectory, GLOBAL_CONFIG_NAME));

        attachToApplication(dexUpdateBean);
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

    public synchronized void attachToApplication(DexUpdateBean dexUpdateBean) {
        List<String> fileList = scanDirector(privateDexDirectory, GLOBAL_CONFIG_NAME);
        if (notSupport) return;
        final List<TwoTuple<String, String>> illegalFileList = new LinkedList<TwoTuple<String, String>>(); // 非法的文件，需要在后面进行删除的
        final List<DexLocalStruct> missingDex = new LinkedList<DexLocalStruct>(); // 缺少的dex，需要进行后继处理的
        final List<DexLocalStruct> replaceDex = new LinkedList<DexLocalStruct>(); // type为0的dex，替换class的dex，需要为立即加载
        final List<DexLocalStruct> addDex = new LinkedList<DexLocalStruct>(); // type为1的dex，添加功能的dex，需要立即加载
        final List<DexLocalStruct> addAsyncDex = new LinkedList<DexLocalStruct>(); // type为2的dex，添加功能的dex，异步加载

        if (null == dexUpdateBean || dexUpdateBean.dexConfigBeans == null || dexUpdateBean.dexConfigBeans.length <= 0) {
            // 没有global.config.json文件, 或者global config为空，则什么都不做
            Log.w(TAG, "Not found config file or config file is empty!");
            return;
        }

        buildDexStruct(fileList, illegalFileList, missingDex, replaceDex, addDex, addAsyncDex, dexUpdateBean);

        loadDex(replaceDex); // 替换dex
        loadDex(addDex); // 添加dex

        DexHotPatchJobHelper.loadAsync(this, addAsyncDex); // 异步加载
        DexHotPatchJobHelper.deleteIllegalFileJob(privateDexDirectory, illegalFileList); // 删除无效的文件
    }

    public void asyncUpdate(DexUpdateBean prevDexUpdateBean, List<DexLocalStruct> localMissingDex, List<DexLocalStruct> addAsyncDex) {
        Job printJob = Jobs.httpGetJson(UPDATE_URL, DexUpdateBean.class);
        printJob.
        .observerOn(null)
                .workOn(Schedulers.computation());
        printJob.observer(new Observer<TwoTuple<Long, Long>, String>() {
            @Override
            public void onNext(Job job, TwoTuple<Long, Long> next) {

            }

            @Override
            public void onCompleted(Job job, String result) {
                Log.d("PrintGetRequest", "onCompleted");
            }

            @Override
            public void onFailed(Job job, Throwable error) {
                Log.d("PrintGetRequest", "onFailed");
            }

            @Override
            public void onCancellation(Job job) {
                Log.d("PrintGetRequest", "onCancellation");
            }
        });

    }

    /**
     * 从fileList中创建
     *
     * @param fileList        输入的所有的文件名称，不包括global.config.json文件
     * @param invalidFileList 失效的文件列表，对应格式为 <[文件名称]----[无效的原因]>
     * @param missingDex
     * @param replaceDex
     * @param addDex
     * @param addAsyncDex
     * @param dexUpdateBean
     */
    private synchronized void buildDexStruct(final List<String> fileList, final List<TwoTuple<String, String>> invalidFileList,
                                             final List<DexLocalStruct> missingDex, final List<DexLocalStruct> replaceDex,
                                             final List<DexLocalStruct> addDex, final List<DexLocalStruct> addAsyncDex,
                                             DexUpdateBean dexUpdateBean) {
        if (null == fileList || fileList.isEmpty() || null == dexUpdateBean
                || null == dexUpdateBean.dexConfigBeans || dexUpdateBean.dexConfigBeans.length <= 0)
            return;
        Set<String> fileSet = new HashSet<String>(fileList);
        final List<DexLocalStruct> totalDexList = new ArrayList<DexLocalStruct>();
        for (DexLocalStruct dexLocalStruct : dexUpdateBean.dexConfigBeans) {
            String name = dexLocalStruct.fileName;
            if (!fileSet.contains(name)) {
                Log.d(TAG, "buildDexStruct, lack of file dexLocalStruct " + dexLocalStruct.toString());
                missingDex.add(dexLocalStruct);
            } else {
                Log.d(TAG, "buildDexStruct, find of file for dexLocalStruct " + dexLocalStruct.toString());
                fileSet.remove(name);
                totalDexList.add(dexLocalStruct);
            }
        }

        { // 判断无效的项
            for (String name : fileSet) { // 未使用的文件
                invalidFileList.add(new TwoTuple<String, String>(name, DexHotPatchConstants.USELESS_FILE));
            }
            // 签名不对的文件
            List<TwoTuple<DexLocalStruct, String>> failedDexList = new ArrayList<TwoTuple<DexLocalStruct, String>>();
            verifyDex(totalDexList, failedDexList, new ArrayList<DexLocalStruct>());
            for (TwoTuple<DexLocalStruct, String> tuple : failedDexList) {
                totalDexList.remove(tuple.value1);
                missingDex.add(tuple.value1);
                invalidFileList.add(new TwoTuple<String, String>(tuple.value1.fileName, tuple.value2));
            }
        }

        deliveryDexStruct(totalDexList, replaceDex, addDex, addAsyncDex);
        order(replaceDex);
        order(addDex);
        order(addAsyncDex);
        totalDexList.clear(); // 清除中间的存储的list
    }


    /**
     * 按照类型进行分类
     *
     * @param inDexList   作为输入的所有的dex list
     * @param replaceDex  type为0的dex list
     * @param addDex      type为1的dex list
     * @param addAsyncDex type为2的dex list
     */
    private static void deliveryDexStruct(List<DexLocalStruct> inDexList, List<DexLocalStruct> replaceDex,
                                          List<DexLocalStruct> addDex, List<DexLocalStruct> addAsyncDex) {
        if (null == inDexList || inDexList.isEmpty()) return;
        for (DexLocalStruct dexLocalStruct : inDexList) {
            switch (dexLocalStruct.type) {
                case 0:
                    replaceDex.add(dexLocalStruct);
                    break;
                case 1:
                    addDex.add(dexLocalStruct);
                    break;
                case 2:
                    addAsyncDex.add(dexLocalStruct);
                    break;
            }
        }
    }

    private static void order(List<DexLocalStruct> dexList) {
        if (null == dexList || dexList.isEmpty()) return;
        Comparator<DexLocalStruct> orderComparator = new Comparator<DexLocalStruct>() {
            @Override
            public int compare(DexLocalStruct lhs, DexLocalStruct rhs) {
                return lhs.fetchTime > rhs.fetchTime ? 1 : -1;
            }
        };
        DexLocalStruct res[] = dexList.toArray(new DexLocalStruct[0]);
        Arrays.sort(res, orderComparator);
        dexList.clear();
        dexList.addAll(Arrays.asList(res));
    }

    public static String sign(File file) {
        return "aaa";
    }

    /**
     * 对dex进行校验，包括dex的sha1匹配，文件校对，config的完整性
     * 可以异步调用
     *
     * @param dexIterable
     * @param failedDexList
     * @param passedDex
     */
    private synchronized void verifyDex(final Iterable<DexLocalStruct> dexIterable,
                                        final List<TwoTuple<DexLocalStruct, String>> failedDexList,
                                        final List<DexLocalStruct> passedDex) {
        if (null == dexIterable) return;
        Iterator<DexLocalStruct> iterator = dexIterable.iterator();
        if (null == iterator) return;
        while (iterator.hasNext()) {
            DexLocalStruct dexLocalStruct = iterator.next();
            if (loadedDexMap.containsValue(dexLocalStruct)) {
                Log.w(TAG, "Want to load a has loaded dex " + dexLocalStruct.toString());
                continue;
            }
            Log.d(TAG, "verifyDex, start calculate sign for file  " + dexLocalStruct.fileName);
            String sign = sign(new File(privateDexDirectory, dexLocalStruct.fileName));
            Log.d(TAG, "verifyDex, complete calculate sign for file  " + dexLocalStruct.fileName + ", sign = " + sign);
            if (dexLocalStruct.dexSign.equals(sign)) {
                passedDex.add(dexLocalStruct);
            } else {
                failedDexList.add(new TwoTuple<DexLocalStruct, String>(dexLocalStruct, DexHotPatchConstants.SIGN_NO_MATCH_ERROR));
            }
        }
    }

    public synchronized void loadDex(Iterable<DexLocalStruct> dexIterable) {
        if (!notSupport) {
            Log.d(TAG, "Not support loadDex!");
            return;
        }
        if (null == dexIterable || null == getApplication()) return;
        final List<TwoTuple<DexLocalStruct, String>> failedDexList = new ArrayList<TwoTuple<DexLocalStruct, String>>();
        final List<TwoTuple<DexLocalStruct, DexConfigBean>> passedDexConfig = new ArrayList<TwoTuple<DexLocalStruct, DexConfigBean>>();
        verifyDex(dexIterable, failedDexList, passedDexConfig);
        notifyFailed(failedDexList);

        if (passedDexConfig.isEmpty()) return;

        ArrayList<File> dexFileList = new ArrayList<File>();
        for (TwoTuple<DexLocalStruct, DexConfigBean> tuple : passedDexConfig) {
            dexFileList.add(new File(privateDexDirectory, tuple.value1.dexFileName));
        }

        try {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
                DexHotPatchInstallHelper.V19.install(getApplication().getClassLoader(), dexFileList, privateDexDirectory);
            } else if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
                DexHotPatchInstallHelper.V14.install(getApplication().getClassLoader(), dexFileList, privateDexDirectory);
            } else {
                // TODO
            }
            notifySuccess(passedDexConfig);
        } catch (IllegalAccessException e) {
            e.printStackTrace();
            notifyFailed(passedDexConfig, e.toString());
        } catch (NoSuchFieldException e) {
            e.printStackTrace();
            notifyFailed(passedDexConfig, e.toString());
        } catch (InvocationTargetException e) {
            e.printStackTrace();
            notifyFailed(passedDexConfig, e.toString());
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
            notifyFailed(passedDexConfig, e.toString());
        }
    }

    private synchronized void notifyFailed(final List<TwoTuple<DexLocalStruct, String>> failedDexList) {
        if (null == observer || null == failedDexList || failedDexList.isEmpty()) return;
        for (TwoTuple<DexLocalStruct, String> entry : failedDexList) {
            observer.onLoadDexFailed(entry.value1.dexFileName, entry.value2);
        }
    }

    private synchronized void notifySuccess(List<TwoTuple<DexLocalStruct, DexConfigBean>> passedDexConfig) {
        if (null == observer || null == passedDexConfig || passedDexConfig.isEmpty()) return;
        for (TwoTuple<DexLocalStruct, DexConfigBean> entry : passedDexConfig) {
            observer.onLoadExtraDex(entry.value1.dexFileName, entry.value2);
        }
    }

    private synchronized void notifyFailed(List<TwoTuple<DexLocalStruct, DexConfigBean>> passedDexConfig, String reason) {
        if (null == observer || null == passedDexConfig || passedDexConfig.isEmpty()) return;
        for (TwoTuple<DexLocalStruct, DexConfigBean> entry : passedDexConfig) {
            observer.onLoadDexFailed(entry.value1.dexFileName, reason);
        }
    }

    public synchronized void fetchRemoteDex(Iterable<DexLocalStruct> remoteDexList) {
        Application application = getApplication();
        if (null == application || null == remoteDexList) return;
        Job job = DexHotPatchJobHelper.downloadDexJob();
        job.observerOn(AndroidMainThreadScheduler.INSTANCE)
                .observer(new Observer.SimpleObserver<Void, DexLocalStruct>() {
                    @Override
                    public void onCompleted(Job job, DexLocalStruct result) {
                        super.onCompleted(job, result);
                    }

                    @Override
                    public void onFailed(Job job, Throwable error) {
                        super.onFailed(job, error);
                    }
                })
                .work();
    }

    public synchronized void deleteFile(String fileName) {
        // TODO
    }

    public void doFinal() {
        // TODO, clean resource, close some stream
    }
}
