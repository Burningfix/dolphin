package org.dolphin.job.operator;

import org.dolphin.http.HttpResponse;
import org.dolphin.job.Operator;
import org.dolphin.lib.IOUtil;

import java.io.InputStream;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class HttpResponseToBytes implements Operator<HttpResponse, byte[]> {
    @Override
    public byte[] operate(HttpResponse input) throws Throwable {
        if(null == input) return null;
        InputStream binaryResource = null;
        try{
//            HttpResponseBody body = input.body();
//            binaryResource = body.getResource().openStream();
            binaryResource = input.body();
            return IOUtil.getBytesFromStream(binaryResource);
        }finally {
            IOUtil.closeQuietly(binaryResource);
            input.close();
        }
    }
}