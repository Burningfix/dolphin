package org.dolphin.hotpatch.apk;

import org.dolphin.lib.progaurd.KeepMemberName;

/**
 * Created by hanyanan on 2015/11/20.
 */
public class GlobalConfigBean implements KeepMemberName {
    public String version;
    public ApkPluginConfig[] apkPluginConfigs;

    public static class ApkPluginConfig implements KeepMemberName{
        public int delayLoad; // 1延时加载，0自动加载
        public String id; // id
        public String sign; // apk的签名
    }
}
