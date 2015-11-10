package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.JobErrorHandler;
import org.dolphin.job.JobRunningResult;
import org.dolphin.job.Operator;
import org.dolphin.job.internal.HttpErrorHandler;
import org.dolphin.job.util.Log;
import org.dolphin.job.Observer;
import org.dolphin.job.operator.PrintTimeOperator;
import org.dolphin.job.schedulers.Schedulers;

import java.util.Random;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class PridiocTester {
    public static void main(String []argv) {
        Job job = new Job(0);
        job.workOn(Schedulers.computation())
                .append(new PrintTimeOperator())
                .append(new Operator() {
                    @Override
                    public Object operate(Object input) throws Throwable {
                        Random random = new Random();
                        if(random.nextBoolean()) throw new Throwable("HAHAHA");
                        return input;
                    }
                })
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
                .handleError(new JobErrorHandler() {
                    @Override
                    public JobRunningResult handleError(Job job, Throwable throwable) {
                        return JobRunningResult.CONTINUE;
                    }
                })
                .workPeriodic(100, 2000, TimeUnit.MILLISECONDS);
    }
}
