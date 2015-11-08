package org.dolphin.job;

import org.dolphin.job.tuple.ThreeTuple;

/**
 * Created by hanyanan on 2015/10/9.
 */
public interface Observer<I, R> {
    public void onNext(Job job, I next);

    public void onCompleted(Job job, R result);

    public void onFailed(Job job, Throwable error);

    public void onCancellation(Job job);


    public static class SimpleObserver<I, R> implements Observer<I, R>{

        @Override
        public void onNext(Job job, I next) {

        }

        @Override
        public void onCompleted(Job job, R result) {

        }

        @Override
        public void onFailed(Job job, Throwable error) {

        }

        @Override
        public void onCancellation(Job job) {

        }
    }
}
