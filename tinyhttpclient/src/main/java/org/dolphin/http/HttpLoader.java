package org.dolphin.http;

import org.dolphin.lib.exception.AbortException;
import java.io.InputStream;
import java.net.HttpURLConnection;


/**
 * Created by hanyanan on 2015/5/13.
 */
public interface HttpLoader<T extends HttpURLConnection> {
    public static final String LOG_TAG = "HttpRequest";
    public static final int HTTP_TEMP_REDIRECT = 307;
    public static final int HTTP_PERM_REDIRECT = 308;
    public static final int MAX_REDIRECT_COUNT = 10;
    public static final String COLONSPACE = ": ";
    public static final String DASHDASH = "--";
    public static final String CRLF = "\r\n";

    public HttpResponse performRequest(HttpRequest httpRequest) throws Throwable;

    /**
     * open url connection
     *
     * @param httpRequest the specify http request
     * @return the http url connection.
     */
    public T openUrlConnection(HttpRequest httpRequest) throws Throwable;

    /**
     * Set the basic attribute to current request;
     *
     * @param httpRequest the specify http request
     * @param connection
     */
    public void setBaseInfo(HttpRequest httpRequest, T connection) throws Throwable;

    /**
     * Set request header property.
     *
     * @param httpRequest the specify http request
     * @param connection
     */
    public void setRequestHeaderProperty(HttpRequest httpRequest, T connection) throws Throwable;

    /**
     * Send request body to server, just useful in "Post" method.
     *
     * @param httpRequest the specify http request
     * @param connection
     */
    public void sendRequestBody(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     * The progress of current upload.
     * @param httpRequest the specify http request
     */
    public void onTransportUpProgress(HttpRequest httpRequest, long cursor, long count) throws AbortException;

    /**
     * Return response http code from server.
     * @param httpRequest
     * @param connection
     */
    public int getResponseCode(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     * Return response http message from server.
     * @param httpRequest
     * @param connection
     */
    public String getResponseMessage(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     * Get the response header from server.
     * @param httpRequest
     * @param connection
     * @return
     */
    public HttpResponseHeader getResponseHeader(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     *
     * @param httpRequest
     * @param connection
     * @return
     * @throws Throwable
     */
    public RedirectedResponse getRedirectedResponse(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     * Redirect to next url. Any throwable will abort current request.
     * @param httpRequest
     * @param redirectedResponse the redirect response.
     */
    public HttpResponse redirectNext(HttpRequest httpRequest, RedirectedResponse redirectedResponse)throws Throwable;

    /**
     * The progress of current download action..
     * @param httpRequest the specify http request
     */
    public void onTransportDownProgress(HttpRequest httpRequest, long cursor, long count) throws AbortException;

    /**
     *
     * @param httpRequest
     * @param connection
     */
    public InputStream getHttpResponseBody(HttpRequest httpRequest, T connection)throws Throwable;

    /**
     *
     * @return
     * @throws Throwable
     */
    public HttpResponse getHttpResponse(HttpRequest httpRequest,  T connection)throws Throwable;

    /**
     *
     * @param httpRequest
     * @return
     */
    public HttpRequestInterceptor getRequestInterceptor(HttpRequest httpRequest);
}
