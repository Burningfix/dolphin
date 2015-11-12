package org.dolphin.dexhotpatch;

import com.google.gson.Gson;

import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.IOException;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/11/11.
 */
public class DexHotPatchJobHelper {
    public final static Gson GSON = new Gson();
    private DexHotPatchJobHelper() {
        throw new UnsupportedOperationException("Cannot instance DexHotPatchJobHelper");
    }

    public static void deleteIllegalFileJob(final File directory, final List<String> fileList) {
        if (null == fileList || fileList.isEmpty()) return;
        Job job = new Job(fileList);
        job.append(new Operator<List<String>, List<String>>() {
            @Override
            public List<String> operate(List<String> input) throws Throwable {
                List<String> failed = new ArrayList<String>();
                for (String fileName : input) {
                    File file = new File(directory, fileName);
                    if (!file.delete()) {
                        failed.add(fileName);
                    }
                }
                return failed;
            }
        })
                .workOn(Schedulers.computation())
                .workDelayed(2, TimeUnit.SECONDS);
    }


    public static void loadAsync(DexHotPatchEngine engine, final List<DexNameStruct> dexNameStructList) {
        if (null == dexNameStructList || dexNameStructList.isEmpty()) return;
        final WeakReference<DexHotPatchEngine> engineWeakReference = new WeakReference<DexHotPatchEngine>(engine);
        Job job = new Job(dexNameStructList);
        job.append(new Operator<List<DexNameStruct>, Object>() {
            @Override
            public Object operate(List<DexNameStruct> input) throws Throwable {
                DexHotPatchEngine engine = engineWeakReference.get();
                if (null != engine) {
                    engine.loadDex(input);
                }
                return null;
            }
        }).workOn(Schedulers.computation()).work();
    }


    public static void update(String url, Map<String, String> params) {

    }





    public static <T> T readFromFile(File file, Class<T> clz){
        try {
            byte[] data = IOUtil.toByteArray(file);
            if(null == data || data.length <= 0) return null;
            String json = new String(data);
            return GSON.fromJson(json, clz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
