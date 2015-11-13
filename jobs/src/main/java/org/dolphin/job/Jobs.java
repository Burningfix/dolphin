package org.dolphin.job;

import com.google.gson.Gson;

import org.dolphin.http.HttpRequest;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.http.HttpOperators;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.PrintLogOperator;
import org.dolphin.job.operator.StringToGson;

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
        job.append(new HttpPerformOperator());
        job.append(new HttpResponseToBytes());
        job.append(new BytesToStringOperator());
        job.append(new PrintLogOperator());
        return job;
    }

    public static <T> Job httpGetJson(String url, final Class<T> clz){
        HttpRequest request = HttpJobs.create(url);
        Job job = new Job(request);
        job.append(new HttpPerformOperator());
        job.append(new HttpResponseToBytes());
        job.append(new BytesToStringOperator());
        job.append(new Operator<String, T>(){
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


    public static Job merge(Job... jobs) {
        return null;
    }

    public static Job zip(Job... jobs) {
        return null;
    }

    public static Job pipeline(Job ... jobs){

        return null;
    }

    public static Job front(Job job){

        return job;
    }

    public static Job back(Job job) {

        return job;
    }
}
