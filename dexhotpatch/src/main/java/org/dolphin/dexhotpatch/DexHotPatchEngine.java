package org.dolphin.dexhotpatch;

import android.app.Application;
import android.content.Context;
import android.os.Build;

import org.dolphin.http.TrafficRecorder;
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
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Created by hanyanan on 2015/11/11.
 * <p/>
 * <p/>
 * 一共有三种dex<br>
 * ---------------------------------------------------------------------------------------------------------------------<br>
 * 0_0_xxxxxxxxx_122222222.dex                      用于取代当前apk中的class，仅用于打补丁，在应用加载时同步执行<br>
 * 0_0_xxxxxxxxx_122222222.config.json              用于0_xxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息<br>
 * 1_2_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用加载时同步执行<br>
 * 1_2_xxxxxxxxx_122222222.config.json              用于1_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层<br>
 * 2_5_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用启动后异步加载<br>
 * 2_5_xxxxxxxxx_122222222.config.json              用于12_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层<br>
 * 第一位0表示取代当前的class，用于线上修复bug；<br>
 * 1代表添加功能需要在app启动时同步进行，用于高优先级页面；<br>
 * 2代表添加功能，需要在app启动后，后台线程进行，由于低优先级页面, 会等到与server通讯完成在进行比对进行加载.<br>
 * 中间数字表示优先级，大小越低，优先级越高，越先加载，最小为0, 决定加载顺序<br>
 * 第三个单元表示是identify，用于dex的key，各个只能有一个<br>
 * <b>第四个单元表示下载的日期，越新的会清除旧的,这个时间是server下发的时间，而非本地时间</b><br>
 * <b>任何合法的都必选且只能包含以上四个部分</b><br>
 * <b>所有可能出现的a-z全小写</b><br>
 * <b>当只存在dex文件，而没有对应的json文件或者json文件无效时，需要删除掉对应的dex和config.json</b><br>
 *<br>
 * 加载流程如下：<br>
 * 1. 从本地读取接口global.config.json, 决定需要加载的dex，如果没有此文件，则等待更新信息在进行加载,跳转到步骤3；<br>
 * 2. 根据步骤1的配置文件加载type为0，1的dex；<br>
 * 3. 从server拉取更新后的global.config.json, 并写入到磁盘，如果有未加载的有效的本地的dex，则加载；<br>
 * 4. 存在有效的未下载的dex，则从服务器拉取后在加载, 对比以前的，只做增量更新，对于已经加载的并且此次无效的，不需要做其他处理。<br>
 */
public class DexHotPatchEngine<T extends Application> {
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
    private final HashMap<String, DexNameStruct> loadedDexMap = new HashMap<String, DexNameStruct>();
    private boolean notSupport = false;
    private DexUpdateBean dexUpdateBean;

    private DexHotPatchEngine(T applicationContext, DexLoadObserver observer) {
        this.applicationRef = new WeakReference<T>(applicationContext);
        this.observer = observer;
        privateDexDirectory = new File(applicationContext.getFilesDir(), DIRECTORY_NAME);
        dexUpdateBean = DexUpdateBean.readFromFile(new File(privateDexDirectory, GLOBAL_CONFIG_NAME));

        attachToApplication();
    }

    // 返回当前保存的application
    protected T getApplication() {
        return applicationRef.get();
    }

    // 只会在当前的目录下的所有file
    private synchronized List<String> scanDirector(File privateDexDirectory) {
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
        String[] fileNameList = privateDexDirectory.list();
        if (null != fileNameList && fileNameList.length <= 0) {
            fileList.addAll(Arrays.asList(fileNameList));
        }

        return fileList;
    }

    public synchronized void attachToApplication(DexUpdateBean expireDexUpdateBean, DexUpdateBean dexUpdateBean) {
        List<String> fileList = scanDirector(privateDexDirectory);
        if (notSupport) return;
        if(null == dexUpdateBean) { // 没有global.config.json文件
            Log.w(TAG, "");
        }

        final List<String> illegalFileList = new LinkedList<String>(); // 非法的文件，需要在后面进行删除的
        final List<DexNameStruct> replaceDex = new LinkedList<DexNameStruct>(); // type为0的dex，替换class的dex，需要为立即加载
        final List<DexNameStruct> addDex = new LinkedList<DexNameStruct>(); // type为1的dex，添加功能的dex，需要立即加载
        final List<DexNameStruct> addAsyncDex = new LinkedList<DexNameStruct>(); // type为2的dex，添加功能的dex，异步加载
        buildDexStruct(fileList, illegalFileList, replaceDex, addDex, addAsyncDex);

        loadDex(replaceDex); // 替换dex
        loadDex(addDex); // 添加dex

        DexHotPatchJobHelper.loadAsync(this, addAsyncDex); // 异步加载
        DexHotPatchJobHelper.deleteIllegalFileJob(privateDexDirectory, illegalFileList); // 删除无效的文件
    }

    private synchronized void buildDexStruct(final List<String> fileList, final List<String> illegalFileList,
                                             final List<DexNameStruct> replaceDex, final List<DexNameStruct> addDex,
                                             final List<DexNameStruct> addAsyncDex) {
        if (null == fileList || fileList.isEmpty()) return;
        final List<DexNameStruct> totalDexList = new ArrayList<DexNameStruct>();
        mergeDexStruct(fileList, illegalFileList, totalDexList);
        deliveryDexStruct(totalDexList, replaceDex, addDex, addAsyncDex);
        order(replaceDex);
        order(addDex);
        order(addAsyncDex);
        totalDexList.clear(); // 清除中间的存储的list
    }


    /**
     * 从fileName中生成dex struct；
     * 同时合并冲突项，具体的是以中间的第三项进行唯一性判断，如果唯一则合并冲突项，合并的原则为新的覆盖旧的
     *
     * @param fileList        所有输入的文件
     * @param illegalFileList 所有非法的需要删除的文件
     * @param totalDexList    所有有效的dex
     */
    private static void mergeDexStruct(final List<String> fileList, final List<String> illegalFileList,
                                       final List<DexNameStruct> totalDexList) {
        final Set<String> fileSet = new HashSet<String>(fileList);
        HashMap<String, DexNameStruct> dexNameStructMap = new HashMap<String, DexNameStruct>();
        while (!fileSet.isEmpty()) {
            String name = fileSet.iterator().next();
            fileSet.remove(name);
            Log.d(TAG, "Find file " + name);
            String prefix = name.substring(0, name.indexOf("."));
            String dexFileName = prefix + DexHotPatchConstants.DEX_SUFFIX;
            String configFileName = prefix + DexHotPatchConstants.CONFIG_SUFFIX;
            if (fileSet.contains(dexFileName) && fileSet.contains(configFileName)) {
                fileSet.remove(dexFileName);
                fileSet.remove(configFileName);
                DexNameStruct dexNameStruct = DexNameStruct.parseDexNameStruct(prefix);
                if (null != dexNameStruct) {
                    DexNameStruct prev = dexNameStructMap.get(dexNameStruct.identify);
                    if (null == prev) { // 新发现一个
                        Log.d(TAG, "Success to get dex file or config file for file " + name);
                        totalDexList.add(dexNameStruct);
                    } else {
                        if (dexNameStruct.fetchTime > prev.fetchTime) { // replace before
                            Log.d(TAG, "Replace before dex struct for " + name);
                            totalDexList.add(dexNameStruct);
                            illegalFileList.add(prev.configFileName);
                            illegalFileList.add(prev.dexFileName);
                            dexNameStructMap.put(dexNameStruct.identify, dexNameStruct);
                        } else { // current has been replaced
                            Log.d(TAG, "Found a  expired dex struct for " + name);
                            illegalFileList.add(dexNameStruct.configFileName);
                            illegalFileList.add(dexNameStruct.dexFileName);
                        }
                    }
                } else {
                    illegalFileList.add(dexFileName);
                    illegalFileList.add(configFileName);
                }
                continue;
            }
            illegalFileList.add(name);
            Log.d(TAG, "Failed to get dex file or config file for file " + name);
        }
    }

    /**
     * 按照类型进行分类
     *
     * @param inDexList   作为输入的所有的dex list
     * @param replaceDex  type为0的dex list
     * @param addDex      type为1的dex list
     * @param addAsyncDex type为2的dex list
     */
    private static void deliveryDexStruct(List<DexNameStruct> inDexList, List<DexNameStruct> replaceDex,
                                          List<DexNameStruct> addDex, List<DexNameStruct> addAsyncDex) {
        if (null == inDexList || inDexList.isEmpty()) return;
        for (DexNameStruct dexNameStruct : inDexList) {
            switch (dexNameStruct.type) {
                case 0:
                    replaceDex.add(dexNameStruct);
                    break;
                case 1:
                    addDex.add(dexNameStruct);
                    break;
                case 2:
                    addAsyncDex.add(dexNameStruct);
                    break;
            }
        }
    }

    private static void order(List<DexNameStruct> dexList) {
        if (null == dexList || dexList.isEmpty()) return;
        Comparator<DexNameStruct> orderComparator = new Comparator<DexNameStruct>() {
            @Override
            public int compare(DexNameStruct lhs, DexNameStruct rhs) {
                return lhs.fetchTime > rhs.fetchTime ? 1 : -1;
            }
        };
        DexNameStruct res[] = dexList.toArray(new DexNameStruct[0]);
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
     * @param passedDexConfig
     */
      private synchronized void verifyDex(Iterable<DexNameStruct> dexIterable,
                                       List<TwoTuple<DexNameStruct, String>> failedDexList,
                                       List<TwoTuple<DexNameStruct, DexConfigBean>> passedDexConfig) {
        if (null == dexIterable) return;
        Iterator<DexNameStruct> iterator = dexIterable.iterator();
        if (null == iterator) return;
        while (iterator.hasNext()) {
            DexNameStruct dexNameStruct = iterator.next();
            if (loadedDexMap.containsValue(dexNameStruct)) {
                Log.w(TAG, "Want to load a has loaded dex " + dexNameStruct.toString());
                continue;
            }
            File configFile = new File(privateDexDirectory, dexNameStruct.configFileName);
            File dexFile = new File(privateDexDirectory, dexNameStruct.dexFileName);
            DexConfigBean configBean = DexConfigBean.readFromFile(configFile);
            if (null == configBean) {
                failedDexList.add(new TwoTuple<DexNameStruct, String>(dexNameStruct, DexHotPatchConstants.CONFIG_READ_ERROR));
            } else {
                String sign = sign(dexFile);
                if (configBean.equals(sign)) {
                    passedDexConfig.add(new TwoTuple<DexNameStruct, DexConfigBean>(dexNameStruct, configBean));
                } else {
                    failedDexList.add(new TwoTuple<DexNameStruct, String>(dexNameStruct, DexHotPatchConstants.SIGN_NO_MATCH_ERROR));
                }
            }
        }
    }

    public synchronized void loadDex(Iterable<DexNameStruct> dexIterable) {
        if (!notSupport) {
            Log.d(TAG, "Not support loadDex!");
            return;
        }
        if (null == dexIterable || null == getApplication()) return;
        final List<TwoTuple<DexNameStruct, String>> failedDexList = new ArrayList<TwoTuple<DexNameStruct, String>>();
        final List<TwoTuple<DexNameStruct, DexConfigBean>> passedDexConfig = new ArrayList<TwoTuple<DexNameStruct, DexConfigBean>>();
        verifyDex(dexIterable, failedDexList, passedDexConfig);
        notifyFailed(failedDexList);

        if (passedDexConfig.isEmpty()) return;

        ArrayList<File> dexFileList = new ArrayList<File>();
        for (TwoTuple<DexNameStruct, DexConfigBean> tuple : passedDexConfig) {
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

    private synchronized void notifyFailed(final List<TwoTuple<DexNameStruct, String>> failedDexList) {
        if (null == observer || null == failedDexList || failedDexList.isEmpty()) return;
        for (TwoTuple<DexNameStruct, String> entry : failedDexList) {
            observer.onLoadDexFailed(entry.value1.dexFileName, entry.value2);
        }
    }

    private synchronized void notifySuccess(List<TwoTuple<DexNameStruct, DexConfigBean>> passedDexConfig) {
        if (null == observer || null == passedDexConfig || passedDexConfig.isEmpty()) return;
        for (TwoTuple<DexNameStruct, DexConfigBean> entry : passedDexConfig) {
            observer.onLoadExtraDex(entry.value1.dexFileName, entry.value2);
        }
    }

    private synchronized void notifyFailed(List<TwoTuple<DexNameStruct, DexConfigBean>> passedDexConfig, String reason) {
        if (null == observer || null == passedDexConfig || passedDexConfig.isEmpty()) return;
        for (TwoTuple<DexNameStruct, DexConfigBean> entry : passedDexConfig) {
            observer.onLoadDexFailed(entry.value1.dexFileName, reason);
        }
    }

    public synchronized void fetchRemoteDex(Iterable<DexUpdateBean.DexEntry> remoteDexList){
        Application application = getApplication();
        if(null == application || null == remoteDexList) return ;
        Iterator<DexUpdateBean.DexEntry> entryIterator = remoteDexList.iterator();

    }

    public synchronized void deleteFile(String fileName){
        // TODO
    }

    public void doFinal() {
        // TODO, clean resource, close some stream
    }
}
