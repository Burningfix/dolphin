package org.dolphin.http.server.dexhotpatch;

import com.google.gson.Gson;

import org.dolphin.http.server.HttpGetRequestHandler;
import org.dolphin.http.server.HttpGetServer;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.lib.util.SecurityUtil;
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
    public static final int PORT = 12345;
    static Gson gson = new Gson();
    public static void main(String []argv) {
        HttpGetServer getServer = new HttpGetServer(PORT);
        HttpGetRequestHandler updateBeanHandler = new HttpGetRequestHandler(){
            @Override
            protected BinaryResource getResource(String path, Map<String, String> params, Map<String, String> responseHeaders) {
                DexUpdateBean dexUpdateBean = new DexUpdateBean();
                dexUpdateBean.version = "1.0";
                dexUpdateBean.identify = "1.0";
                DexLocalStruct dexLocalStruct = new DexLocalStruct();
                dexLocalStruct.fileName = "123456.dex";
                dexLocalStruct.type = 0;
                dexLocalStruct.url = "http://172.18.16.47:"+PORT+"/dex";
                try {
                    dexLocalStruct.dexSign = SecurityUtil.sha1(IOUtil.toByteArray(new File("D:\\multidex\\classes.dex")));
                } catch (IOException e) {
                    e.printStackTrace();
                }
                dexUpdateBean.dexConfigBeans = new DexLocalStruct[]{dexLocalStruct};
                byte[] beans = gson.toJson(dexUpdateBean).getBytes();
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
                return new FileBinaryResource(new File("D:\\multidex\\classes.dex"));
            }
        };
        getServer.registerRequestHandler("/dex", dexHandler);
        getServer.start();
    }
}
