package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.util.Log;
import org.dolphin.job.Observer;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class PrintGetRequest {
    public static void main(String []argv) {
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
}
