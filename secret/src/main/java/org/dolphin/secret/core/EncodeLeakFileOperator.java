package org.dolphin.secret.core;

import android.util.Log;

import org.dolphin.job.Operator;
import org.dolphin.job.tuple.TwoTuple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/15.
 * <p/>
 * 加密多个文件
 */
public class EncodeLeakFileOperator implements Operator<List<String>, List<TwoTuple<ObscureFileInfo, FileInfoContentCache>>> {
    public static final String TAG = "EncodeLeakFileOperator";
    private final File rootDir;

    public EncodeLeakFileOperator(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public List<TwoTuple<ObscureFileInfo, FileInfoContentCache>> operate(List<String> input) throws Throwable {
        if (null == input || input.isEmpty()) {
            return null;
        }
        ObscureOperator operator = new ObscureOperator();
        List<TwoTuple<ObscureFileInfo, FileInfoContentCache>> res = new ArrayList<TwoTuple<ObscureFileInfo, FileInfoContentCache>>();
        for (String fileName : input) {
            try {
                TwoTuple<ObscureFileInfo, FileInfoContentCache> tuple = operator.operate(new File(rootDir, fileName));
                res.add(new TwoTuple<ObscureFileInfo, FileInfoContentCache>(tuple.value1, tuple.value2));
                Log.i(TAG, "Encode file success: " + fileName);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Log.w(TAG, "Failed encode file " + fileName);
            }
        }
        return res;
    }
}
