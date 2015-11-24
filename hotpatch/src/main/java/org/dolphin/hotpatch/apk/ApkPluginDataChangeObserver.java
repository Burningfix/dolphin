package org.dolphin.hotpatch.apk;

import org.dolphin.hotpatch.apk.ApkPlugin;

import java.util.List;

/**
 * Created by hanyanan on 2015/11/23.
 */
public interface ApkPluginDataChangeObserver {
    public void onApkPluginDataChanged(final List<ApkPlugin> apkPluginList);
}
