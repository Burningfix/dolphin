package org.dolphin.http.server;

import org.dolphin.lib.IOUtil;
import org.dolphin.lib.ValueUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.FileBinaryResource;

import java.io.*;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/3.
 * 客户端请求如下：Range: bytes=5275648-
 * 服务器返回：Content-Length=106786028
 * Accept-Ranges=bytes
 * Content-Range=bytes 2000070-106786027/106786028
 * 如果使用Request带有Range,则返回206,
 * 否则返回200
 */
public class HttpGetRequestHandler implements HttpRequestHandler {
    private static final String TEST_FILE = "C:\\Users\\Public\\Music\\Sample Music\\Sleep Away.mp3";


    protected BinaryResource getResource(String path, Map<String, String> params, Map<String, String> responseHeaders) {
        return new FileBinaryResource(new File(TEST_FILE));
    }

    @Override
    public BinaryResource handle(String path, Map<String, String> params, Map<String, String> headers,
                                 Map<String, String> responseHeaders) throws IOException {
        responseHeaders.put("Content-Type", "application/octet-stream");
        responseHeaders.put("Server", "Tiny Http Server/0.1");
        responseHeaders.put(HttpGetServer.CODE_KEY, String.valueOf(200));

        final BinaryResource binaryResource = getResource(path, params, responseHeaders);

        String rangeString = headers.get("Range");
        if (null == rangeString) {
            rangeString = headers.get("RANGE");
        }
        responseHeaders.put("Content-Range", String.valueOf(binaryResource.size()));
        if (!ValueUtil.isEmpty(rangeString)) {
            final long[] range = TinyHttpHelper.getRange(rangeString);
            if (range != null && range.length == 2 && range[0] >= 0) {
                responseHeaders.put(HttpGetServer.CODE_KEY, String.valueOf(206));
                responseHeaders.put("Accept-Ranges", "bytes");
                final long resourceMostSize = binaryResource.size();
                range[1] = range[1] > range[0] ? range[1] : resourceMostSize - 1;
                range[1] = range[1] > resourceMostSize - 1 ? resourceMostSize - 1 : range[1];
                final long transportSize = range[1] - range[0] + 1;

                BinaryResource out = new BinaryResource() {
                    @Override
                    public InputStream openStream() throws IOException {
                        InputStream inputStream = binaryResource.openStream();
                        if(range[0] > 0) {
                            IOUtil.skip(inputStream, range[0]);
                        }
                        return inputStream;
                    }

                    @Override
                    public byte[] read() throws IOException {
                        return binaryResource.read();
                    }

                    @Override
                    public long size() {
                        return transportSize;
                    }
                };

                //Content-Range=bytes 2000070-106786027/106786028
                String contentRange = "bytes " + range[0] + "-" + range[1] + "/" + resourceMostSize;
                responseHeaders.put("Content-Range", contentRange);
                responseHeaders.put("Content-Length", String.valueOf(transportSize));
                HttpLog.d("Handler", "Get path " + path + "Content-Range["+contentRange+"]");
                return out;
            }
        }


        return binaryResource;
    }
}
