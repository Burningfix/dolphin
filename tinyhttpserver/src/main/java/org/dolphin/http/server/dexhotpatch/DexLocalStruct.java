package org.dolphin.http.server.dexhotpatch;

import com.google.gson.Gson;

/**
 * Created by hanyanan on 2015/11/12.
 */
class DexLocalStruct {
    public static final Gson gson = new Gson();
    String fileName;
    int type; //0冷部署，1热部署
    int priority; // 0-无穷大
    String url;
    long fetchTime; // 从服务器获取时间,时间为服务器时间
    String dexSign; // dex的sha1签名
    String name; // dex的原始名称
    String desc; // 该dex的描述
    String config; // json格式的config


    public byte[] toBytes(){
        return gson.toJson(this).getBytes();
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
