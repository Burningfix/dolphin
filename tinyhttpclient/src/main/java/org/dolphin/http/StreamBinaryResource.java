package org.dolphin.http;

import java.io.IOException;
import java.io.InputStream;

import hyn.com.lib.IOUtil;
import hyn.com.lib.binaryresource.BinaryResource;

/**
 * Created by hanyanan on 2015/5/26.
 * Notes that, it just support consume once.
 */
public class StreamBinaryResource implements BinaryResource {
    private final InputStream inputStream;
    private final long size;
    private byte[] data;
    private boolean consumed = false;
    public StreamBinaryResource(InputStream stream, long size){
        this.inputStream = stream;
        this.size = size;
    }
    @Override
    public InputStream openStream() throws IOException {
        if(null != data) {
            return IOUtil.bytesToInputStream(data);
        }
        return inputStream;


        //        if(!consumed) {
//            consumed = true;
//            return inputStream;
//        }
//
//        if(null == data) {
//            throw new IllegalStateException("The resource has consumed!");
//        }
//
//        return IOUtil.bytesToInputStream(data);
    }

    @Override
    public byte[] read() throws IOException {
        if(!consumed){
            data = IOUtil.getBytesFromStream(inputStream);
            consumed = true;
            IOUtil.safeClose(inputStream);
            return data;
        }

        if(data != null) return data;

        throw new IllegalStateException("The resource has consumed!");
    }

    @Override
    public long size() {
        return size;
    }
}
