package org.dolphin.http;

import org.dolphin.lib.exception.AbortException;

import java.io.IOException;

/**
 * Created by hanyanan on 2015/9/15.
 */
public interface HttpRequestInterceptor {
    /**
     * Prepare package request params.
     * In <b>Get</b> method need to generate a complete url, and <b>Post</b> method need to package all params to
     * request body.
     *
     * @param request the specify http request on running.
     * @throws InterruptedException interrupt current request executing.
     */
    public void onPrepareRunning(HttpRequest request) throws InterruptedException;

    /**
     * After set request header properties.
     *
     * @param request the specify http request on running.
     * @throws InterruptedException interrupt current request executing.
     */
    public void onWriteRequestHeaderFinish(HttpRequest request) throws InterruptedException;

    /**
     * The progress callback for current upload transport.
     *
     * @param request  the specify http request on running.
     * @param position the cursor of current position.
     * @param count    the size of body need send to server.
     * @throws InterruptedException interrupt current request executing.
     * @throws IOException io exception occured..
     */
    public void onTransportUpProgress(HttpRequest request, long position, long count) throws AbortException;

    /**
     * After request body has been send complete.
     *
     * @param request the specify http request on running.
     * @throws InterruptedException interrupt current request executing.
     */
    public void onAfterWriteRequestBody(HttpRequest request) throws InterruptedException;

    /**
     * The callback when get the response code from server.
     *
     * @param request the specify http request on running.
     * @param code    the response code from server
     * @return the response code.
     * @throws InterruptedException interrupt current request executing.
     */
    public int onReadResponseCode(HttpRequest request, int code) throws InterruptedException;

    /**
     * The callback after read the response from server.
     *
     * @param request        the specify http request on running.
     * @param responseHeader the response header from server
     * @return return the finally response header
     * @throws InterruptedException interrupt current request executing.
     */
    public HttpResponseHeader onReadResponseHeader(HttpRequest request, HttpResponseHeader responseHeader) throws InterruptedException;

    /**
     * Prepare redirect to the next url, invoke this method after finish a redirect request and before redirect to the
     * specify url. The imlments may be throw a InterruptedException to interrupted current request..
     * <br/>
     * If current request support jump to next url, then will call {@link #onPrepareRunning(HttpRequest)} function.
     * @param request            the specify http request on running.
     * @param redirectedResponse the redirectedResponse parsed from server.
     * @return the target redirectedResponse
     * @throws InterruptedException to abort current request
     */
    public RedirectedResponse onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse, int currCount) throws InterruptedException;

    /**
     * The progress callback for current download transport.
     *
     * @param request  the specify http request on running.
     * @param position the cursor of current position.
     * @param count    the size of body need send to server.
     * @throws InterruptedException interrupt current request executing.
     */
    public void onTransportDownProgress(HttpRequest request, long position, long count) throws AbortException;

    /**
     * The callback after get request body from server.
     *
     * @param request      the specify http request on running.
     * @param responseBody the body received from server.
     * @return the finally body received from server.
     * @throws InterruptedException interrupt current request exception.
     */
    public HttpResponseBody onReadRequestBodyFinish(HttpRequest request, HttpResponseBody responseBody) throws InterruptedException;

    /**
     * The callback after finish current request.
     *
     * @param request  the specify http request on running.
     * @param response the response get from server.
     * @return the finally response.
     * @throws InterruptedException interrupt current request exception.
     */
    public HttpResponse onAfterRunning(HttpRequest request, HttpResponse response) throws InterruptedException;

    /**
     * A base http loader, that has add log
     */
    public abstract class BaseHttpLoader implements HttpRequestInterceptor {
        @Override
        public void onPrepareRunning(HttpRequest request) throws InterruptedException {

        }

        @Override
        public void onWriteRequestHeaderFinish(HttpRequest request) throws InterruptedException {

        }

        @Override
        public void onTransportUpProgress(HttpRequest request, long position, long count) throws AbortException {

        }

        @Override
        public void onAfterWriteRequestBody(HttpRequest request) throws InterruptedException {

        }

        @Override
        public int onReadResponseCode(HttpRequest request, int code) throws InterruptedException {
            return code;
        }

        @Override
        public HttpResponseHeader onReadResponseHeader(HttpRequest request, HttpResponseHeader responseHeader) throws InterruptedException {
            return responseHeader;
        }

        @Override
        public RedirectedResponse onPrepareRedirect(HttpRequest request, RedirectedResponse redirectedResponse, int currCount) throws InterruptedException {
            return redirectedResponse;
        }

        @Override
        public void onTransportDownProgress(HttpRequest request, long position, long count) throws AbortException {

        }

        @Override
        public HttpResponseBody onReadRequestBodyFinish(HttpRequest request, HttpResponseBody responseBody) throws InterruptedException {
            return responseBody;
        }

        @Override
        public HttpResponse onAfterRunning(HttpRequest request, HttpResponse response) throws InterruptedException {
            return response;
        }
    }
}
