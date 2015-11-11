package org.dolphin.dexhotpatch;

import android.app.Application;
import android.content.Context;

import org.dolphin.job.util.Log;

import java.io.File;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/11.
 * <p/>
 * <p/>
 * 一共有三种dex
 * ---------------------------------------------------------------------------------------------------------------------
 * 0_0_xxxxxxxxx_122222222.dex                      用于取代当前apk中的class，仅用于打补丁，在应用加载时同步执行
 * 0_0_xxxxxxxxx_122222222.config.json              用于0_xxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息
 * 1_2_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用加载时同步执行
 * 1_2_xxxxxxxxx_122222222.config.json              用于1_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层
 * 2_5_xxxxxxxxx_122222222.dex                      用于添加额外的功能，在应用启动后异步加载
 * 2_5_xxxxxxxxx_122222222.config.json              用于12_xxxxxxxxx_122222222.dex的配置信息，至少包括dex的SHA1信息，还有其他的config信息，需要传递到应用层
 * 第一位0表示取代当前的class，用于线上修复bug；
 * 1代表添加功能需要在app启动时同步进行，用于高优先级页面；
 * 2代表添加功能，需要在app启动后，后台线程进行，由于低优先级页面
 * 中间数字表示优先级，大小越低，优先级越高，越先加载，最小为0
 * 第三个单元表示是identify，用于dex的key，各个只能有一个
 * 第四个单元表示下载的日期，越新的会清楚旧的
 * 任何合法的都必选且只能包含以上四个部分
 * 所有可能出现的a-z全小写
 */
public class DexHotPatchEngine<T extends Application> {
    private static final String DIRECTORY_NAME = "dex_private_dir";
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
    private final HashMap<String, String> loadedDexMap = new HashMap<String, String>();
    private final List<String> fileList = new ArrayList<String>();
    private boolean notSupport = false;

    private DexHotPatchEngine(T applicationContext, DexLoadObserver observer) {
        this.applicationRef = new WeakReference<T>(applicationContext);
        this.observer = observer;
        privateDexDirectory = new File(applicationContext.getFilesDir(), DIRECTORY_NAME);
        scanDirector(privateDexDirectory);
        if (notSupport) return;

        attachToApplication();
    }

    private synchronized void scanDirector(File privateDexDirectory) {
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

        String[] fileNameList = privateDexDirectory.list();
        if (null != fileNameList && fileNameList.length <= 0) {
            fileList.addAll(Arrays.asList(fileNameList));
        }
    }



    public synchronized void attachToApplication() {

    }


    private static class DexNameStruct implements Comparable<DexNameStruct> {
        private int type; //0,1,2
        private int priority; // 0-无穷大
        private String identify; // dex的标识
        public long fetchTime; // 从服务器获取时间

        @Override
        public int compareTo(DexNameStruct dexNameStruct) {
            return 0;
        }
    }

    public static interface DexLoadObserver {
        public void onLoadExtraDex(String dexName, String config);

        public void onLoadReplaceDex(String dexName, List<String> pathList);

        public void onLoadDexFailed(String dexName, String reason);
    }
}
