package org.dolphin.http.server;

import org.dolphin.lib.util.DateUtils;
import org.dolphin.lib.util.IOUtil;
import org.dolphin.lib.util.ValueUtil;
import org.dolphin.lib.binaryresource.BinaryResource;
import org.dolphin.lib.binaryresource.ByteArrayBinaryResource;

import java.io.*;
import java.net.*;
import java.util.HashMap;
import java.util.Map;

/**
 * Created by hanyanan on 2015/11/3.
 */
public class HttpGetServer {
    /**
     * 对应请求的http方法，例如GET
     */
    public static final String METHOD_KEY = "Method" + System.nanoTime();

    /**
     * 请求的完整的url，例如/n?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528
     */
    public static final String RAW_PATH_KEY = "RAW_PATH" + System.nanoTime();

    /**
     * 请求的路径，例如 n/b/v
     */
    public static final String PATH_KEY = "PATH" + System.nanoTime();

    /**
     * 请求的协议的， 对应例如：http/1.1
     */
    public static final String PROTOCOL_KEY = "Protocol" + System.nanoTime();

    /**
     * server返回的code
     */
    public static final String CODE_KEY = "Code" + System.nanoTime();

    private static final String TAG = "HttpGetServer";
    private static final String LINE_DIVIDER = "\r\n";
    private boolean start = false;
    private int port;
    private final Map<String, HttpRequestHandler> requestHandlers = new HashMap<String, HttpRequestHandler>();
    private ServerSocket localServer;
    private Thread serverThread = null;

    public HttpGetServer(int port) {
        this.port = port;
    }

    /**
     * http请求的path， 比如http://127.0.0.1:8456/aa/bb.c/aaa?a=b&c=d, 则path为'aa/bb.c/aaa'
     *
     * @param path    请求的path
     * @param handler 处理的handler
     */
    public void registerRequestHandler(String path, HttpRequestHandler handler) {
        requestHandlers.put(path, handler);
    }

    public int getPort() {
        return this.port;
    }

    public synchronized void start() {
        start = true;
        serverThread = new Thread() {
            public void run() {
                while (true) {
                    if (!start) return;
                    try {
                        localServer = new ServerSocket(getPort());
                        localServer.setReuseAddress(false);
                        while (true) {
                            Socket localSocket = localServer.accept();
                            HttpLog.d(TAG, "new address " + localSocket.getPort());
                            doRequest(localSocket);
                        }
                    } catch (IOException e) {
                        e.printStackTrace();
                        ++port;
                    }
                }
            }
        };
        serverThread.start();
    }

    public synchronized void stop() {
        start = false;
        try {
            localServer.close();
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            localServer = null;
        }
        serverThread.interrupt();
        serverThread = null;
    }


    //Http 请求的头部如下
    //GET /n?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528 HTTP/1.1\r\n
    //Host: news.baidu.com\r\n
    //Connection: keep-alive\r\n
    //Accept: image/webp,image/*,*/*;q=0.8\r\n
    //User-Agent: Mozilla/5.0 (Windows NT 6.1; WOW64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/46.0.2490.80 Safari/537.36\r\n
    //Referer: http://news.baidu.com/static/fisp_static/common/module_static_include/module_static_include_6ccc067.css\r\n
    //Accept-Encoding: gzip, deflate, sdch\r\n
    //Accept-Language: zh-CN,zh;q=0.8,en;q=0.6\r\n
    //Cookie: PSTM=1441610484; BAIDUID=867C2B35C57DF9F138C87BB46702A10D:FG=1;\r\n
    //\r\n
    //返回的包含请求的header，请求的path，协议，方法
    protected Map<String, String> readRequestHeader(InputStream inputStream) throws IOException {
        BufferedInputStream streamReader = new BufferedInputStream(inputStream);
        BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(streamReader, "utf-8"));
        String line = null;
        int i = 0;
        Map<String, String> headMap = new HashMap<String, String>();
        while ((line = bufferedReader.readLine()) != null) {
//            HttpLog.d(TAG, "Read request line [ " + line + " ]");
            if (line.length() <= 0) break;
            if (i == 0) {
                // decode line: GET /n?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528 HTTP/1.1\r\n
                String[] paths = line.split(" ");
                if (paths.length < 2) {
                    HttpLog.e(TAG, "can not get url path");
                    break;
                }
                headMap.put(PROTOCOL_KEY, paths[2]); // HTTP/1.1
                headMap.put(METHOD_KEY, paths[0]); // GET
                headMap.put(RAW_PATH_KEY, paths[1]); // /n?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528
            } else {
                int index = line.indexOf(":");
                if (index <= 0) continue;

                headMap.put(line.substring(0, index), line.substring(index + 1)); // http request header
            }
            ++i;
        }

        return headMap;
    }

    /**
     * 解析请求的url, 返回包含path和param的mapping。path格式如下：a/b/c，对应key为{@link #PATH_KEY}
     *
     * @param path 例如/n/c/d?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528
     * @return path（n/c/d）和请求的参数mapping
     */
    protected Map<String, String> parseRequestPath(String path) {
        final Map<String, String> out = new HashMap<String, String>();
        int index = path.indexOf('?');
        if (index <= 0) {
            out.put(PATH_KEY, path);
            return out;
        }

        out.put(PATH_KEY, path.substring(0, index));
        if (index >= 0 && index < path.length()) {
            path = path.substring(index + 1);
        }
        if (ValueUtil.isEmpty(path)) {
            return out;
        }

        String[] params = path.split("&");
        if (null != params) {//parse request params
            for (String p : params) {
                if (p == null) continue;
                String[] pp = p.split("=");
                if (pp.length != 2) continue;
                out.put(URLDecoder.decode(pp[0]), URLDecoder.decode(pp[1]));
            }
        }

        return out;
    }

    /**
     * HTTP/1.1 200 OK\r\n
     * Content-Encoding: gzip\r\n
     * Content-Type: text/html\r\n
     * Date: Tue, 03 Nov 2015 06:47:38 GMT\r\n
     * Server: Apache\r\n
     * Vary: Accept-Encoding\r\n
     * Transfer-Encoding: chunked\r\n
     *
     * @param responseResource
     * @param headers
     * @param outputStream
     * @throws IOException
     */
    public static void sendResponse(BinaryResource responseResource, int responseCode, Map<String, String> headers,
                                    OutputStream outputStream) throws IOException {
        StringBuilder sb = new StringBuilder();
        int code = responseCode;
        sb.append("HTTP/1.1 ").append(responseCode).append(" ")
                .append(TinyHttpHelper.getHttpDesc(code))
                .append(LINE_DIVIDER);
        for (Map.Entry<String, String> entry : headers.entrySet()) {
//			if("Content-Range".equalsIgnoreCase(entry.getKey())){
//				sb.append(entry.getKey()).append("=").append(entry.getValue()).append(LINE_DIVIDER);
//			}else{
            sb.append(entry.getKey()).append(": ").append(entry.getValue()).append(LINE_DIVIDER);
//			}
        }
        sb.append("Connection: close").append(LINE_DIVIDER);
        sb.append("Date: " + DateUtils.getCurrentTime()).append(LINE_DIVIDER);
        sb.append(LINE_DIVIDER);
        outputStream.write(sb.toString().getBytes());
        // 传输body，但是传输不超responseResource的size，
        // 对于没有Range: bytes=5275648-15275648d的请求，{@code responseResource.size()}的大小为文件的大小
        // 对于存在Range: bytes=5275648-15275648d的请求，{@code responseResource.size()}的大小为可以传输的大小
        IOUtil.copy(responseResource.openStream(), outputStream, responseResource.size());
        outputStream.flush();
    }

    private void doRequest(Socket localSocket) {
        new Thread(new HttpGetServerRunnable(localSocket, requestHandlers)).start();
    }


    public class HttpGetServerRunnable implements Runnable {
        private final Socket socket;
        private final Map<String, HttpRequestHandler> requestHandlers = new HashMap<String, HttpRequestHandler>();

        public HttpGetServerRunnable(Socket socket, Map<String, HttpRequestHandler> handlerMap) {
            this.socket = socket;
            this.requestHandlers.putAll(handlerMap);
        }

        public void run() {
            HttpLog.d(TAG, "..........localSocket connected..........");
            InputStream inStream = null;
            OutputStream outStream = null;
            try {
                socket.setKeepAlive(false);
                inStream = socket.getInputStream();
                outStream = socket.getOutputStream();
                Map<String, String> requestHeader = readRequestHeader(inStream);
                String method = requestHeader.remove(METHOD_KEY); // GET
                String protocol = requestHeader.remove(PROTOCOL_KEY); // Http/1.1
                String fullUrl = requestHeader.remove(RAW_PATH_KEY); // /n/b/c?m=rddata&v=index_data&rn1=17&callback=bdNewsJsonCallBack&ra=0.6327211381867528
                socket.shutdownInput();
                HttpLog.d(TAG, "[" + method + " " + fullUrl + " " + protocol + "]");
                if (!"GET".equalsIgnoreCase(method)) {
                    sendResponse(new ByteArrayBinaryResource("just support Get Method!"), 400, ERROR_HEADER, outStream);
                    return;
                }
                printHeader(requestHeader);
                Map<String, String> paths = parseRequestPath(fullUrl); // 得到当前路径和请求参数
                String path = paths.remove(PATH_KEY); // 请求的path，不包含请求参数
                HttpRequestHandler handler = requestHandlers.get(path);
                if (null == handler) {
                    sendResponse(new ByteArrayBinaryResource("Not Found"), 404, ERROR_HEADER, outStream);
                    return;
                }

                Map<String, String> responseHeader = new HashMap<String, String>();
                BinaryResource resource = handler.handle(path, paths, requestHeader, responseHeader);
                int code = Integer.valueOf(responseHeader.remove(CODE_KEY));
                sendResponse(resource, code, responseHeader, outStream);
            } catch (IOException exception) {
//                sendResponse(new ByteArrayBinaryResource("Server Internal Error"), 500, ERROR_HEADER, outStream);
            } finally {
                IOUtil.closeQuietly(inStream);
                IOUtil.closeQuietly(outStream);
                IOUtil.closeQuietly(socket);
            }

            HttpLog.d(TAG, "..........localSocket disconnected..........");
        }
    }

    public static void printHeader(Map<String, String> headers) {
        if (null == headers) return;
        for (Map.Entry<String, String> entry : headers.entrySet()) {
            HttpLog.d(TAG, "[" + entry.getKey() + ":" + entry.getValue() + "]");
        }
    }

    public static final Map<String, String> ERROR_HEADER = new HashMap<String, String>();

    static {

    }
}
