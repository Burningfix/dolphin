package org.dolphin.hotpatch.apk;

import org.dolphin.lib.progaurd.KeepClassName;
import org.dolphin.lib.progaurd.KeepMemberName;

/**
 * Created by hanyanan on 2015/11/20.
 */
public class ApkPluginInterface implements KeepClassName, KeepMemberName {
    public String descriptor;
    public String id;
    public String name;
    public String path;
    public String extensionConfig;
}
