package org.dolphin.rv;

/**
 * Created by hanyanan on 2015/9/18.
 */
public interface RvErrorHandler {
    public RvObservable onError(RvObservable observable, Throwable throwable);
}
