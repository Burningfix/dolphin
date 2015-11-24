package org.dolphin.hotpatch.apk.server;

import com.google.gson.Gson;

import org.dolphin.hotpatch.apk.GlobalConfigBean;
import org.dolphin.http.server.HttpGetRequestHandler;
import org.dolphin.http.server.HttpGetServer;
import org.dolphin.lib.IOUtil;
import org.dolphin.lib.SecurityUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;
import org.dolphin.lib.binaryresource.FileBinaryResource;

import java.io.File;
import java.io.IOException;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/16.
 */
public class Server {
    public static final int PORT = 23456;
    static Gson gson = new Gson();
    public static void main(String []argv) {
        HttpGetServer getServer = new HttpGetServer(PORT);
        HttpGetRequestHandler updateBeanHandler = new HttpGetRequestHandler(){
            @Override
            protected BinaryResource getResource(String path, Map<String, String> params, Map<String, String> responseHeaders) {
                GlobalConfigBean globalConfigBean = new GlobalConfigBean();
                GlobalConfigBean.ApkPluginConfig apkPluginConfig = new GlobalConfigBean.ApkPluginConfig();
                apkPluginConfig.delayLoad = 0;
                apkPluginConfig.id = "213";
                apkPluginConfig.sign = "222";
                globalConfigBean.version = "1.0";
                globalConfigBean.apkPluginConfigs = new GlobalConfigBean.ApkPluginConfig[1];
                globalConfigBean.apkPluginConfigs[0] = apkPluginConfig;
                byte[] beans = gson.toJson(globalConfigBean).getBytes();
                byte[] res = new byte[beans.length + 1];
                System.arraycopy(beans, 0, res, 1, beans.length);
                res[0] = 1;
                return new ByteArrayBinaryResource(res);
            }
        };
        getServer.registerRequestHandler("/update", updateBeanHandler);

        HttpGetRequestHandler dexHandler = new HttpGetRequestHandler(){
            @Override
            protected BinaryResource getResource(String path, Map<String, String> params, Map<String, String> responseHeaders) {
                return new FileBinaryResource(new File("D:\\hotwork\\dolphin\\hotpatchpluginapp\\build\\outputs\\apk\\hotpatchpluginapp1_sign.apk"));
            }
        };
        getServer.registerRequestHandler("/apk", dexHandler);
        getServer.start();
    }
}
