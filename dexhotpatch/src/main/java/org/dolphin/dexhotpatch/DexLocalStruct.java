package org.dolphin.dexhotpatch;

import com.google.gson.Gson;

import org.dolphin.lib.ValueUtil;

/**
 * Created by hanyanan on 2015/11/12.
 */
class DexLocalStruct implements Comparable<DexLocalStruct> {
    public static final Gson gson = new Gson();
    String fileName;
    int type; //0,1,2
    int priority; // 0-无穷大
    String identify; // dex的标识
    long fetchTime; // 从服务器获取时间,时间为服务器时间
    String dexSign; // dex的sha1签名
    String dexName; // dex的原始名称
    String desc; // 该dex的描述
    String config; // json格式的config


    public byte[] toBytes(){
        return gson.toJson(this).getBytes();
    }

    @Override
    public int compareTo(DexLocalStruct dexLocalStruct) {
        if (!dexLocalStruct.identify.equalsIgnoreCase(identify)) {
            throw new IllegalArgumentException("Key not match, forbid");
        }
        return this.fetchTime > dexLocalStruct.fetchTime ? 1 : -1;
    }


    @Override
    public int hashCode() {
        return fileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if(null == o) return false;
        if(this == o) return true;
        if(DexLocalStruct.class.isInstance(o)) {
            DexLocalStruct dex = (DexLocalStruct)o;
            return identify.equals(dex.identify);
        }
        return false;
    }

//    /**
//     * 从一个dex文件名称中推测出相应的{@link DexLocalStruct}数据结构
//     * @param namePrefix 在private目录中得到的file name的前缀
//     * @return 如果合法的话返回dexNameStruct，非法的话则直接返回null
//     */
//    public static DexLocalStruct parseDexNameStruct(final String namePrefix) {
//        if(ValueUtil.isEmpty(namePrefix)) return null;
//        String []block = namePrefix.split("_");
//        if(null == block || block.length != 4) return null;
//        DexLocalStruct dexLocalStruct = new DexLocalStruct();
//        dexLocalStruct.type = ValueUtil.parseInt(block[0], 1);
//        dexLocalStruct.priority = ValueUtil.parseInt(block[1], 0);
//        dexLocalStruct.identify = block[2];
//        dexLocalStruct.fetchTime = ValueUtil.parseLong(block[3], 0);
//        dexLocalStruct.configFileName = namePrefix + DexHotPatchConstants.CONFIG_SUFFIX;
//        return dexLocalStruct;
//    }
}
