package org.dolphin.hotpatch.dex;

import com.google.gson.Gson;

import org.dolphin.http.HttpGetLoader;
import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.job.util.Log;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.SecurityUtil;
import org.dolphin.lib.ValueUtil;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.util.List;

/**
 * Created by hanyanan on 2015/11/11.
 */
public class DexHotPatchJobHelper {
    public static final String TAG = DexHotPatchEngine.TAG;

    public final static Gson GSON = new Gson();

    private DexHotPatchJobHelper() {
        throw new UnsupportedOperationException("Cannot instance DexHotPatchJobHelper");
    }

    public static boolean isSignMatch(DexLocalStruct dexLocalStruct, File file) {
        String sign = dexLocalStruct.dexSign;
        if (ValueUtil.isEmpty(sign)) return true;
        try {
            String fileSign = SecurityUtil.sha1(IOUtil.toByteArray(file));
            if (ValueUtil.isEmpty(fileSign)) return false;
            return sign.equalsIgnoreCase(fileSign);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return false;
    }

    public static void downLoadDexListIfNeed(File dir, List<DexLocalStruct> downloadIfNeedList, List<DexLocalStruct> failedList) {
        if (null == downloadIfNeedList || downloadIfNeedList.isEmpty()) return;
        for (DexLocalStruct struct : downloadIfNeedList) {
            File file = new File(dir, struct.fileName);
            if (file.exists() && file.canRead() && !file.isDirectory() && file.canWrite()) {
                if (DexHotPatchJobHelper.isSignMatch(struct, file)) {
                    Log.d(TAG, "downLoadDexListIfNeed, disk has " + struct.toString() + " and it is valid, no need to download from server!");
                    continue;
                } else {
                    Log.d(TAG, "downLoadDexListIfNeed " + struct.toString() + " is invalid(No such file or sign unMatch), need to download from server!");
                    file.delete();
                }
            }
            String url = struct.url;
            HttpRequest request = new HttpRequest(url);
            HttpResponse response = null;
            OutputStream outputStream = null;
            try {
                response = HttpGetLoader.INSTANCE.performRequest(request);
                File outFile = new File(dir, struct.fileName);
                outputStream = new FileOutputStream(outFile);
                IOUtil.copy(response.body(), outputStream);
                outputStream.flush();
                if (!isSignMatch(struct, outFile)) {
                    Log.e(DexHotPatchEngine.TAG, "downLoadDexListIfNeed, " + struct.toString() + " sign checked, Not Match, is Failed!");
                    failedList.add(struct);
                } else {
                    Log.d(DexHotPatchEngine.TAG, "downLoadDexListIfNeed, " + struct.toString() + " sign checked, is OK!");
                }
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                failedList.add(struct);
            } finally {
                IOUtil.closeQuietly(request);
                IOUtil.closeQuietly(outputStream);
                IOUtil.closeQuietly(response);
            }
        }
    }


    public static <T> T readFromFile(File file, Class<T> clz) {
        try {
            byte[] data = IOUtil.toByteArray(file);
            if (null == data || data.length <= 0) return null;
            String json = new String(data);
            return GSON.fromJson(json, clz);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }

    public static <T> T readFromBytes(byte[] data, int offset, int length, Class<T> clz) {
        try {
            String json = new String(data, offset, length);
            return GSON.fromJson(json, clz);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}
