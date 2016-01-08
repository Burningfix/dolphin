package org.dolphin.job.sample;

import org.dolphin.job.Job;
import org.dolphin.job.Log;
import org.dolphin.job.operator.PrintTimeOperator;
import org.dolphin.job.schedulers.Schedulers;

/**
 * Created by hanyanan on 2015/10/27.
 */
public class PriorityTester {
    public static int[] sDelayTime = new int[]{
            300, 300, 300, 300, 300,
            100, 100, 100, 100, 100,
            1000, 1000, 1000, 1000, 1000,
            800, 800, 800, 800, 800,

    };

    public static void main(String[] argv) {
        Job jobs[] = new Job[sDelayTime.length];
        for (int i = 0; i < jobs.length; ++i) {
            jobs[i] = new Job(i);
            final Job job = jobs[i];
            jobs[i].workOn(Schedulers.computation())
                    .then(new PrintTimeOperator())
                    .result(new Job.Callback1() {
                        @Override
                        public void call(Object result) {
                            Log.d("", "onCompleted " + job.toString());
                        }
                    })
                    .workDelayed(sDelayTime[i]);
        }
    }
}
