package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.Operator;
import org.dolphin.job.Log;
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
                .then(new PrintTimeOperator())
                .then(new Operator() {
                    @Override
                    public Object operate(Object input) throws Throwable {
                        Random random = new Random();
                        if (random.nextBoolean()) throw new Throwable("HAHAHA");
                        return input;
                    }
                })
                .error(new Job.Callback2() {
                    @Override
                    public void call(Throwable throwable, Object[] unexpectedResult) {
                        Log.d("", "Throwable " + throwable.getMessage());
                    }
                })
                .workPeriodic(100, 2000, TimeUnit.MILLISECONDS);
    }
}
