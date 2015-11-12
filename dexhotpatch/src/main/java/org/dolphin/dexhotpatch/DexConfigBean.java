package org.dolphin.dexhotpatch;

import com.google.gson.Gson;

import org.dolphin.lib.IOUtil;

import java.io.File;
import java.io.IOException;

/**
 * Created by hanyanan on 2015/11/12.
 */
public class DexConfigBean {
    public static Gson sGson = new Gson();
    public String dexSign; // dex的sha1签名
    public String dexName; // dex的原始名称
    public String desc; // 该dex的描述
    public String config; // json格式的config
    public String []classes;


    public static DexConfigBean readFromFile(File configFile){
        try {
            byte[] data = IOUtil.toByteArray(configFile);
            String config = new String(data);
            return sGson.fromJson(config, DexConfigBean.class);
        } catch (IOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
