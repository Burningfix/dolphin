package org.dolphin.secret.http;

import org.dolphin.http.server.HttpGetRequestHandler;
import org.dolphin.http.server.HttpGetServer;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.secret.browser.BrowserManager;
import org.dolphin.secret.core.ObscureFileInfo;
import org.dolphin.secret.core.ReadableFileInputStream;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Map;

/**
 * Created by hanyanan on 2016/5/9.
 */
public class HttpServer {
    public static final int DEFAULT_PORT = 13433;
    public static final String FILE_PATH = "/file";
    private final HttpGetServer httpGetServer;
    private boolean isStart = false;
    private int port = DEFAULT_PORT;
    private HttpGetRequestHandler requestHandler = new HttpGetRequestHandler() {
        @Override
        protected BinaryResource getResource(String path, Map<String, String> params, Map<String, String> responseHeaders) {
            String id = params.get("file");
            final ObscureFileInfo fileInfo = HttpContainer.getInstance().getFileInfo(id);
            return new BinaryResource() {
                @Override
                public InputStream openStream() throws IOException {
                    return new ReadableFileInputStream(new File(BrowserManager.sRootDir, fileInfo.obscuredFileName), fileInfo);
                }

                @Override
                public byte[] read() throws IOException {
                    throw new NullPointerException();
                }

                @Override
                public long size() {
                    return fileInfo.originalFileLength;
                }
            };
        }
    };

    public HttpServer() {
        httpGetServer = new HttpGetServer(port);
        httpGetServer.registerRequestHandler(FILE_PATH, requestHandler);
        isStart = false;
    }

    public void start() {
        httpGetServer.start();
        isStart = true;
    }

    public void stop() {
        isStart = false;
        httpGetServer.stop();
    }

    public boolean isStart() {
        return this.isStart;
    }

    public String wrapObscurePath(String id) {
        return "http://127.0.0.1:" + port + FILE_PATH + "?file=" + id;
    }
}
