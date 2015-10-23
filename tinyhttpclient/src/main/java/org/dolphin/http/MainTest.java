package org.dolphin.http;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;

import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/9/16.
 */
public class MainTest {
    public static void main(String[] argv) {
        HttpPostLoader postLoader = new HttpPostLoader();
        HttpGetLoader getLoader = new HttpGetLoader();
        String url = "http://httpbin.org/post";
        HttpRequest request = new HttpRequest(url, Method.POST);
        Map<String, String> params = new HashMap<String, String>();
        params.put("cityid", "100010000");
        params.put("url", "http://httpbin.org/get");
        request.params(params);
        HttpRequestBody body = new HttpRequestBody();
        body.add("json", new ByteArrayBinaryResource("{data:dddddddddddddddddddddddddddddddddddddddddd}".getBytes()));
        request.setHttpRequestBody(body);
        try {
            HttpResponse response = postLoader.performRequest(request);
            HttpLog.d("TinyHttpClient", response.getResponseHeader().string());
            if (!response.isSuccessful()) {
                System.out.println(response.toString());
                return;
            }
            BinaryResource resource = response.body().getResource();
            InputStream stream = resource.openStream();
            byte[] data = IOUtil.getBytesFromStream(stream);
            System.out.println(new String(data));
            IOUtil.closeQuietly(stream);
        } catch (Throwable throwable) {
            throwable.printStackTrace();
        }
    }
}
