package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.util.Log;
import org.dolphin.job.Observer;
import org.dolphin.job.operator.PrintTimeOperator;
import org.dolphin.job.schedulers.Schedulers;

import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class PridiocTester {
    public static void main(String []argv) {
        Job job = new Job(0);
        job.workOn(Schedulers.COMPUTATION_SCHEDULER)
                .append(new PrintTimeOperator())
                .observer(new Observer() {
                    @Override
                    public void onNext(Job job, Object next) {

                    }

                    @Override
                    public void onCompleted(Job job, Object result) {

                    }

                    @Override
                    public void onFailed(Job job, Throwable error) {
                        Log.d("job", "onFailed");
                    }

                    @Override
                    public void onCancellation(Job job) {

                    }
                })
                .workPeriodic(100, 300, TimeUnit.MILLISECONDS);
        try {
            Thread.sleep(3000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        job.abort();
    }
}
