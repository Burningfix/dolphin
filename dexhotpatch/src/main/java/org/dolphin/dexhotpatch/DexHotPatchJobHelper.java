package org.dolphin.dexhotpatch;

import com.google.gson.Gson;

import org.dolphin.http.HttpGetLoader;
import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.util.Log;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.lang.ref.WeakReference;
import java.util.ArrayList;
import java.util.HashMap;
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

    public static boolean isSignMatch(DexLocalStruct dexLocalStruct, File file) {
        // TODO
        return true;
    }

    public static void downLoadDexList(File dir, List<DexLocalStruct> structList, List<DexLocalStruct> failedList) {
        if(null == structList || structList.isEmpty()) return ;
        for(DexLocalStruct struct : structList){
            String url = struct.url;
            HttpRequest request = new HttpRequest(url);
            HttpResponse response = null;
            OutputStream outputStream = null;
            try {
                response = HttpGetLoader.INSTANCE.performRequest(request);
                File outFile = new File(dir, struct.fileName);
                outputStream = new FileOutputStream(outFile);
                IOUtil.copy(response.body(), outputStream);
                if(!isSignMatch(struct, outFile)){
                    Log.e(DexHotPatchEngine.TAG, struct.toString() + " sign checked, is Failed!");
                    failedList.add(struct);
                }else{
                    Log.d(DexHotPatchEngine.TAG, struct.toString() + " sign checked, is OK!");
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                failedList.add(struct);
            }finally {
                IOUtil.closeQuietly(request);
                IOUtil.closeQuietly(outputStream);
                IOUtil.closeQuietly(response);
            }
        }
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

    public static <T> T readFromBytes(byte[]data, int offset, int length, Class<T> clz){
        try {
            String json = new String(data, offset, length);
            return GSON.fromJson(json, clz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
