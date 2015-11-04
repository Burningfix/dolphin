package org.dolphin.job.internal;

import org.dolphin.job.Job;
import org.dolphin.job.Observer;

/**
 * Created by hanyanan on 2015/10/13.
 */
public class ObserverList implements Observer {
    @Override
    public void onNext(Job job, Object next) {

    }

    @Override
    public void onCompleted(Job job, Object result) {

    }

    @Override
    public void onFailed(Job job, Throwable error) {

    }

    @Override
    public void onCancellation(Job job) {

    }
}
