package org.dolphin.job;

import com.google.gson.Gson;

import org.dolphin.http.HttpRequest;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.PrintLogOperator;

import java.util.Map;

/**
 * Created by hanyanan on 2015/10/14.
 */
public class Jobs {








    /**
     * 创建一个pending的Job，每次
     *
     * @return
     */
    public static Job pending() {

        return null;
    }


    public static <T> Job create(T input) {

        return null;
    }

    public static Job httpGet(String url) {
        HttpRequest request = HttpJobs.create(url);
        Job job = new Job(request);
        job.then(new HttpPerformOperator());
        job.then(new HttpResponseToBytes());
        job.then(new BytesToStringOperator());
        job.then(new PrintLogOperator());
        return job;
    }

    public static <T> Job httpGetJson(String url, final Class<T> clz) {
        HttpRequest request = HttpJobs.create(url);
        Job job = new Job(request);
        job.then(new HttpPerformOperator());
        job.then(new HttpResponseToBytes());
        job.then(new BytesToStringOperator());
        job.then(new Operator<String, T>() {
            @Override
            public T operate(String input) throws Throwable {
                Gson gson = new Gson();
                return gson.fromJson(input, clz);
            }
        });
        return job;
    }

    public static Job httpGet(String url, Map<String, String> params) {

        return null;
    }

    public static Job httpPost(String url) {

        return null;
    }


    /**
     * 将一个或多个job
     */
    public static Job zip(Iterable<Job> jobIterable) {
        return null;
    }

    /**
     * 一个Job的输出作为下一个job的输入
     */
    public static Job pipeline(Iterable<Job> jobIterable) {

        return null;
    }

    /**
     * 将一个或多个job<b>并起来</b>, 使Job能够并行
     */
    public static Job bunch(Iterable<Job> jobIterable) {

        return null;
    }
}
