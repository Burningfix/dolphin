package org.dolphin.dexhotpatch;

import org.dolphin.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/11/12.
 */
class DexNameStruct implements Comparable<DexNameStruct> {
    String dexFileName;
    String configFileName;
    int type; //0,1,2
    int priority; // 0-无穷大
    String identify; // dex的标识
    long fetchTime; // 从服务器获取时间,时间为服务器时间

    @Override
    public int compareTo(DexNameStruct dexNameStruct) {
        if (!dexNameStruct.identify.equalsIgnoreCase(identify)) {
            throw new IllegalArgumentException("Key not match, forbid");
        }
        return this.fetchTime > dexNameStruct.fetchTime ? 1 : -1;
    }


    @Override
    public int hashCode() {
        return dexFileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(null == o) return false;
        if(this == o) return true;
        if(DexNameStruct.class.isInstance(o)) {
            DexNameStruct dex = (DexNameStruct)o;
            return dexFileName.equals(dex.dexFileName);
        }
        return false;
    }

    /**
     * 从一个dex文件名称中推测出相应的{@link DexNameStruct}数据结构
     * @param namePrefix 在private目录中得到的file name的前缀
     * @return 如果合法的话返回dexNameStruct，非法的话则直接返回null
     */
    public static DexNameStruct parseDexNameStruct(final String namePrefix) {
        if(ValueUtil.isEmpty(namePrefix)) return null;
        String []block = namePrefix.split("_");
        if(null == block || block.length != 4) return null;
        DexNameStruct dexNameStruct = new DexNameStruct();
        dexNameStruct.type = ValueUtil.parseInt(block[0], 1);
        dexNameStruct.priority = ValueUtil.parseInt(block[1], 0);
        dexNameStruct.identify = block[2];
        dexNameStruct.fetchTime = ValueUtil.parseLong(block[3], 0);
        dexNameStruct.configFileName = namePrefix + DexHotPatchConstants.CONFIG_SUFFIX;
        dexNameStruct.dexFileName = namePrefix + DexHotPatchConstants.DEX_SUFFIX;
        return dexNameStruct;
    }
}
