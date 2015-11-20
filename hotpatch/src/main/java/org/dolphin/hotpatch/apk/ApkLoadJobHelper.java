package org.dolphin.hotpatch.apk;

import com.google.gson.Gson;

import org.dolphin.hotpatch.AndroidMainThreadScheduler;
import org.dolphin.http.HttpRequest;
import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Operator;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.PrintLogOperator;
import org.dolphin.job.operator.SwallowExceptionOperator;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by hanyanan on 2015/11/20.
 */
public class ApkLoadJobHelper {
    public static final Gson gson = new Gson();

    private ApkLoadJobHelper() throws IllegalAccessException {
        throw new IllegalAccessException();
    }

    public static <T> Job createUpdateJob(String url, final File defaultFile, final Class<T> clazz) {
        HttpRequest request = new HttpRequest(url);
        Job updateJob = new Job(request);
        updateJob.append(new SwallowExceptionOperator(new HttpPerformOperator(), null));
        updateJob.append(new SwallowExceptionOperator(new HttpResponseToBytes(), null));
        updateJob.append(new Operator<byte[], T>() {
            @Override
            public T operate(byte[] input) throws Throwable {
                if (null == input || input.length <= 0) return null;
                if (input[0] == 0) { // 从文件中读取
                    return swallowExceptionParse(readFromFile(defaultFile), clazz);
                }
                // 从server 中读取
                IOUtil.write(defaultFile, input, 1, input.length - 1);
                return swallowExceptionParse(input, 1, input.length - 1, clazz);
            }
        });
        updateJob.workOn(Schedulers.computation());
        updateJob.observerOn(AndroidMainThreadScheduler.INSTANCE);
        return updateJob;
    }

    public static <T> T swallowExceptionParse(byte[] data, int offset, int length, Class<T> clazz) {
        if (0 == length || null == data || data.length <= 0) return null;
        try {
            return gson.fromJson(new String(data, offset, length), clazz);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return null;
    }

    public static <T> T swallowExceptionParse(byte[] data, Class<T> clazz) {
        if (null == data || data.length <= 0) return null;
        return swallowExceptionParse(data, 0, data.length, clazz);
    }

    public static byte[] readFromFile(File file) {
        try {
            return IOUtil.toByteArray(file);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return null;
    }
}
