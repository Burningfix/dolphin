package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.http.BaseHttpObserver;
import org.dolphin.job.schedulers.Schedulers;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class PrintGetRequest {
    public static void main(String []argv) {
        Job printJob = Jobs.httpGet("http://httpbin.org/get");
        printJob.observerOn(null);
        printJob.workOn(Schedulers.COMPUTATION_SCHEDULER);
        printJob.observer(new BaseHttpObserver());
    }
}
