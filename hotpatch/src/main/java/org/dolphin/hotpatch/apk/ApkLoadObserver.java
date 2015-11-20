package org.dolphin.hotpatch.apk;

/**
 * Created by hanyanan on 2015/11/19.
 */
public interface ApkLoadObserver {
    public void onApkLoaded(ApkPlugin apkPlugin);
    public void onApkLoadFailed(String id, Throwable throwable);
}
