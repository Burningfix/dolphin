package org.dolphin.job.sample;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpResponse;
import org.dolphin.job.*;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.util.Log;
import org.dolphin.job.operator.*;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;

import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;

/**
 * Created by yananh on 2015/10/25.
 */
public class SingleThreadDownloadTest {
    public static void runStringDownload(){
        Job printJob = Jobs.httpGet("http://httpbin.org/get");
        printJob.observerOn(null);
        printJob.workOn(Schedulers.COMPUTATION_SCHEDULER);
        printJob.observer(new Observer<TwoTuple<Long, Long>, String>() {
            @Override
            public void onNext(Job job, TwoTuple<Long, Long> next) {

            }

            @Override
            public void onCompleted(Job job, String result) {
                Log.d("PrintGetRequest", "onCompleted");
            }

            @Override
            public void onFailed(Job job, Throwable error) {
                Log.d("PrintGetRequest", "onFailed");
            }

            @Override
            public void onCancellation(Job job) {
                Log.d("PrintGetRequest", "onCancellation");
            }
        });
    }

    public static void runBlockDownload(){
        String url = "http://mirror.cyanogenmod.org/jenkins/128821/cm-11-20151004-NIGHTLY-n7100-recovery.img";
        HttpRequest request = HttpJobs.create(url);
        Job job = new Job(request);
        job.append(new HttpPerformOperator());
        Operator read = new Operator<HttpResponse, InputStream>(){

            @Override
            public InputStream operate(HttpResponse input) throws Throwable {
                return input.body().getResource().openStream();
            }
        };

        Operator write = new Operator<HttpResponse, OutputStream>(){

            @Override
            public OutputStream operate(HttpResponse input) throws Throwable {
                return new FileOutputStream("D:\\a.dat");
            }
        };

        Operator size = new Operator<HttpResponse, Long>(){

            @Override
            public Long operate(HttpResponse input) throws Throwable {
                return input.body().getResource().size();
            }
        };

        List<Operator> operatorList = new ArrayList<Operator>();
        operatorList.add(read);
        operatorList.add(write);
        operatorList.add(size);
        job.merge(operatorList.iterator());
        job.until(new StreamCopyOperator(), true);
        job.observer(new Observer<TwoTuple<Long, Long>, Object>() {
            @Override
            public void onNext(Job job, TwoTuple<Long, Long> next) {
                if(null != next) {
                    Log.d("runBlockDownload", "next [" +next.value1 + " - " + next.value2+"]");
                }
            }

            @Override
            public void onCompleted(Job job, Object result) {
                Log.d("runBlockDownload", "onCompleted");
            }

            @Override
            public void onFailed(Job job, Throwable error) {
                Log.d("runBlockDownload", "onFailed");
            }

            @Override
            public void onCancellation(Job job) {
                Log.d("runBlockDownload", "onCancellation");
            }
        });







    }

    public static void main(String []argv) {
//        runStringDownload();
        runBlockDownload();
    }
}
