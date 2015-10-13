package org.dolphin.http;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;

import java.io.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/9/16.
 */
public class MainDownloadTest {
    public static void main(String[] argv) {
        HttpGetLoader getLoader = new HttpGetLoader();
        String url = "http://cdimage.ubuntu.com/ubuntukylin/releases/14.04.2/release/ubuntukylin-14.04.2-desktop-amd64.iso";
        HttpRequest request = new HttpRequest(url, Method.GET);
        try {
            HttpResponse response = getLoader.performRequest(request);
            BinaryResource resource = response.body().getResource();
            InputStream stream = resource.openStream();
            OutputStream fileOut = new FileOutputStream(new File("D:\\a.exe"));
            IOUtil.copy(stream, fileOut);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
