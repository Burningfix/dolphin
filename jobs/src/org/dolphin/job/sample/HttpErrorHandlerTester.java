package org.dolphin.job.sample;

import org.dolphin.http.HttpRequest;
import org.dolphin.job.Job;
import org.dolphin.job.Jobs;
import org.dolphin.job.Observer;
import org.dolphin.job.Operator;
import org.dolphin.job.http.HttpJobs;
import org.dolphin.job.internal.HttpErrorHandler;
import org.dolphin.job.operator.BytesToStringOperator;
import org.dolphin.job.operator.HttpPerformOperator;
import org.dolphin.job.operator.HttpResponseToBytes;
import org.dolphin.job.operator.PrintLogOperator;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.job.util.Log;

/**
 * Created by hanyanan on 2015/10/28.
 */
public class HttpErrorHandlerTester {
    public static void main(String[] argv) {
        HttpRequest request = HttpJobs.create("http://httpbin.org/get");
        Job job = new Job(request);
        job.append(new Operator() { // 手动产生异常
            int count = 0;

            @Override
            public Object operate(Object input) throws Throwable {
                count++;
                if (count < 5) {
                    throw new RuntimeException("Create a runtime exception for testing, count " + count);
                }
                return input;
            }
        })
                .append(new HttpPerformOperator())
                .append(new HttpResponseToBytes())
                .append(new BytesToStringOperator())
                .append(new PrintLogOperator())
                .observerOn(null)
                .handleError(new HttpErrorHandler())
                .workOn(Schedulers.computation())
                .work();
        job.observer(new Observer<TwoTuple<Long, Long>, String>() {
            @Override
            public void onNext(Job job, TwoTuple<Long, Long> next) {

            }

            @Override
            public void onCompleted(Job job, String result) {
                Log.d("HttpErrorHandlerTester", "onCompleted");
            }

            @Override
            public void onFailed(Job job, Throwable error) {
                Log.d("HttpErrorHandlerTester", "onFailed");
            }

            @Override
            public void onCancellation(Job job) {
                Log.d("HttpErrorHandlerTester", "onCancellation");
            }
        });
    }
}
