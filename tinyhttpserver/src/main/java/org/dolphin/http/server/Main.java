package org.dolphin.http.server;


/**
 * Created by hanyanan on 2015/11/3.
 */
public class Main {
    public static final String QUERY_FILE_LIST_PATH = "/query";
    public static final String QUERY_FILE_PARAM_KEY = "file";
    public static final String QUERY_FILE_PARAM_TYPE_KEY = "type"; // audio, video, photo, all
    public static final String REQUEST_FILE_PATH="/get";

    public static void main(String []argv) {
        HttpGetServer getServer = new HttpGetServer(8877);
        QueryFilesRequestHandler handler = new QueryFilesRequestHandler("D:\\book");
        getServer.registerRequestHandler(QUERY_FILE_LIST_PATH, handler);
        getServer.registerRequestHandler(REQUEST_FILE_PATH, handler);
        getServer.start();
    }
}
