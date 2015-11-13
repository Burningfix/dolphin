package org.dolphin.dexhotpatch;

import java.io.File;

/**
 * Created by hanyanan on 2015/11/12.
 *
 * 从服务器得到的更新的配置文件
 */
public class DexUpdateBean {
    public String version;
    public String identify; // 唯一标识，如果没有更新，则不需要改动
    public DexLocalStruct[] dexConfigBeans; // 有效的dex集合

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (null == o) return false;
        if (DexUpdateBean.class.isInstance(o)) {
            DexUpdateBean other = (DexUpdateBean) o;
            return this.identify.equals(other.identify);
        }
        return false;
    }

    public static DexUpdateBean readFromFile(File file) {
        return DexHotPatchJobHelper.readFromFile(file, DexUpdateBean.class);
    }
}
