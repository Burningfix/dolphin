package org.dolphin.secret.core;

import android.util.Log;

import org.dolphin.job.Operator;
import org.dolphin.job.tuple.TwoTuple;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2016/2/15.
 *
 * 加密多个文件
 */
public class EncodeLeakFileOperator implements Operator<List<String>, List<TwoTuple<FileInfo, FileInfoContentCache>>> {
    public static final String TAG = "EncodeLeakFileOperator";
    private final File rootDir;

    public EncodeLeakFileOperator(File rootDir) {
        this.rootDir = rootDir;
    }

    @Override
    public List<TwoTuple<FileInfo, FileInfoContentCache>> operate(List<String> input) throws Throwable {
        if (null == input || input.isEmpty()) {
            return null;
        }
        FileEncodeOperator operator = new FileEncodeOperator();
        List<TwoTuple<FileInfo, FileInfoContentCache>> res = new ArrayList<TwoTuple<FileInfo, FileInfoContentCache>>();
        for (String fileName : input) {
            try {
                TwoTuple<FileInfo, FileInfoContentCache> tuple = operator.operate(new File(rootDir, fileName));
                res.add(new TwoTuple<FileInfo, FileInfoContentCache>(tuple.value1, tuple.value2));
                Log.i(TAG, "Encode file success: " + fileName);
            } catch (Throwable throwable) {
                throwable.printStackTrace();
                Log.w(TAG, "Failed encode file " + fileName);
            }
        }
        return res;
    }
}
