package org.dolphin.rv.subscribe;

import org.dolphin.rv.RvSubscriber;

/**
 * Created by hanyanan on 2015/9/23.
 */
public class RvSubscribers {

    private RvSubscribers(){
        throw new IllegalAccessError("Cannot build a instance.");
    }


    public static final RvSubscriber EMPTY_SUBSCRIBER = new RvSubscriber(){
        public void onNext(Object t) {

        }

        @Override
        public void onError(Throwable error) {

        }

        @Override
        public void onCompleted(Object result) {

        }
    };



}
