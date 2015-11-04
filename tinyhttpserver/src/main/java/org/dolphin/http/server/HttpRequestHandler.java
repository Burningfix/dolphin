package org.dolphin.http.server;

import org.dolphin.lib.binaryresource.BinaryResource;

import java.io.IOException;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/3.
 */
public interface HttpRequestHandler {
    public BinaryResource handle(String path, Map<String, String> params, Map<String,String> headers,
                                 Map<String, String> responseHeaders) throws IOException;
}
