package org.dolphin.job.http;

import org.dolphin.http.HttpResponse;
import org.dolphin.job.Job;
import org.dolphin.job.util.Log;
import org.dolphin.job.tuple.TwoTuple;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class BaseHttpObserver implements Observer<TwoTuple<Long, Long>, HttpResponse> {
    public static final String TAG = "HttpObserver";

    @Override
    public void onNext(Job job, TwoTuple<Long, Long> next) {
        if (null != next) {
            Log.d(TAG, "[" + next.value1 + " - " + next.value2 + "] " + job.toString());
        }
    }

    @Override
    public void onCompleted(Job job, HttpResponse result) {

    }

    @Override
    public void onFailed(Job job, Throwable error) {

    }

    @Override
    public void onCancellation(Job job) {

    }
}
