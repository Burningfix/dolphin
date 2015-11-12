package org.dolphin.dexhotpatch;

import java.io.File;

/**
 * Created by hanyanan on 2015/11/12.
 */
public class DexUpdateBean {
    public String version;
    public DexEntry []dexEntries; // 有效的dex集合

    public static class DexEntry {
        public int type;
        public String identify;
        public long fetchTime;
        public String url;
    }

    public static DexUpdateBean readFromFile(File file){
        return DexHotPatchJobHelper.readFromFile(file, DexUpdateBean.class);
    }

//    public boolean contain(DexNameStruct ){
//
//    }
}
