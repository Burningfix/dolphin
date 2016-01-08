package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Log;
import org.dolphin.job.schedulers.Schedulers;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class PrintGetRequest {
    public static void main(String []argv) {
        Job printJob = Jobs.httpGet("http://httpbin.org/get");
        printJob.observerOn(null);
        printJob.workOn(Schedulers.computation());
        printJob.result(new Job.Callback1() {
            @Override
            public void call(Object result) {
                Log.d("PrintGetRequest", "onCompleted");
            }
        }).error(new Job.Callback2() {
            @Override
            public void call(Throwable throwable, Object[] unexpectedResult) {
                Log.d("PrintGetRequest", "onFailed");
            }
        }).cancel(new Job.Callback0() {
            @Override
            public void call() {
                Log.d("PrintGetRequest", "onCancellation");
            }
        });
    }
}
