package org.dolphin.hotpatch.apk;

import org.dolphin.job.tuple.ThreeTuple;
import org.dolphin.lib.progaurd.KeepClassName;
import org.dolphin.lib.progaurd.KeepMemberName;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by hanyanan on 2015/11/20.
 */
public class ApkPluginInterface implements KeepClassName, KeepMemberName {
    public String descriptor;
    public String id;
    public String name;
    public String extensionConfig;
    public List<PageSpec> pageSpecList = new ArrayList<PageSpec>();

    public ApkPluginInterface(){

    }

    public static interface PageSpec{
        public String name();
        public String id();
        public String path();
        public boolean hasOption();
    }
}
