package org.dolphin.http;

import org.dolphin.lib.ValueUtil;
import org.dolphin.lib.exception.AbortException;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;
import static org.dolphin.lib.Preconditions.checkNotNull;

/**
 * Created by dolphin on 2015/5/22.
 */
public class HttpUrlLoader<T extends HttpURLConnection> implements HttpLoader<T> {
    public HttpResponse performRequest(HttpRequest request) throws Throwable {
        checkNotNull(request);
        T connection = null;
        try {
            connection = openUrlConnection(request);
            setBaseInfo(request, connection);
            setRequestHeaderProperty(request, connection);
            sendRequestBody(request, connection);
            connection.connect(); // connection to server
            int statusCode = getResponseCode(request, connection);
            if (isRedirect(statusCode)) { // redirect to next url
                RedirectedResponse redirectedResponse = getRedirectedResponse(request, connection);
                connection.disconnect();
                return redirectNext(request, redirectedResponse);
            } else {
                return getHttpResponse(request, connection);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw e;
        } catch (AbortException e){
            e.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw e;
        } catch (MalformedURLException e) {
            e.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw e;
        } catch (IOException e) {
            e.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw e;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw throwable;
        }
    }

    @Override
    public T openUrlConnection(HttpRequest httpRequest) throws Throwable {
        String url = getUrl(httpRequest);
        URL addressUrl = new URL(url);
        T connection = (T) addressUrl.openConnection();
        return connection;
    }

    @Override
    public void setBaseInfo(HttpRequest httpRequest, T connection) throws Throwable {
        connection.setRequestMethod(httpRequest.methodString());
        connection.setInstanceFollowRedirects(true);
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }

    @Override
    public void setRequestHeaderProperty(HttpRequest request, T connection) throws Throwable {
        HttpRequestHeader requestHeader = request.getRequestHeader();
        if (null == requestHeader) return;
        Map<String, String> headers = requestHeader.maps();
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        if (null == entrySet) return;
        for (Map.Entry<String, String> entry : entrySet) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    @Override
    public void sendRequestBody(HttpRequest httpRequest, T connection) throws Throwable {
        // default method do nothing
    }

    @Override
    public void onTransportUpProgress(HttpRequest httpRequest, long cursor, long count) throws AbortException {
        HttpLog.d("tinyHttpClient", httpRequest.toString() + " Up "+cursor+" - " + count);
        TrafficRecorder trafficRecorder = httpRequest.getTrafficStatus();
        if (null != trafficRecorder) {
            // TODO
        }
    }

    @Override
    public int getResponseCode(HttpRequest httpRequest, T connection) throws Throwable {
        return connection.getResponseCode();
    }

    @Override
    public String getResponseMessage(HttpRequest httpRequest, T connection) throws Throwable {
        return connection.getResponseMessage();
    }

    @Override
    public HttpResponseHeader getResponseHeader(HttpRequest httpRequest, T connection) throws Throwable {
        connection.getResponseCode();
        return new HttpResponseHeader(connection.getHeaderFields());
    }

    @Override
    public RedirectedResponse getRedirectedResponse(HttpRequest request, T connection) throws Throwable {
        HttpResponseHeader header = getResponseHeader(request, connection);
        String forwardUrl = header.getForwardUrl();
        return new RedirectedResponse(connection.getResponseCode(), connection.getResponseMessage(), forwardUrl, header);
    }

    @Override
    public HttpResponse redirectNext(HttpRequest httpRequest, RedirectedResponse redirectedResponse) throws Throwable {
        String forwardUrl = redirectedResponse.getForwardUrl();
        httpRequest.setForwardUrl(forwardUrl);
        HttpLog.d("TinyHttpClient", httpRequest.toString() + " Forward To " + forwardUrl);
        return performRequest(httpRequest); //redirect to next request
    }

    @Override
    public void onTransportDownProgress(HttpRequest httpRequest, long cursor, long count) throws AbortException {
        // TODO
        HttpLog.d("tinyHttpClient", httpRequest.toString() + " Down "+cursor+" - " + count);
    }

    @Override
    public HttpResponseBody getHttpResponseBody(final HttpRequest httpRequest, T connection) throws Throwable {
        final long contentLength = connection.getContentLengthLong();// TODO
        InputStream inputStream = connection.getInputStream();
        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(inputStream, connection) {
            @Override
            protected void onRead(long readCount) throws IOException, AbortException {
                onTransportDownProgress(httpRequest, readCount, contentLength);
            }
        };

        return new HttpResponseBody(new StreamBinaryResource(inputStreamWrapper, contentLength));
    }

    @Override
    public HttpResponse getHttpResponse(HttpRequest request, T connection) throws Throwable {
        int statusCode = getResponseCode(request, connection);
        String msg = getResponseMessage(request, connection);
        HttpResponse.Builder builder = new HttpResponse.Builder(request);
        if (isSuccess(statusCode)) {
            HttpResponseHeader header = getResponseHeader(request, connection);
            HttpResponseBody responseBody = getHttpResponseBody(request, connection);
            builder.setMessage(msg);
            builder.setStatusCode(statusCode);
            builder.setBody(responseBody);
            builder.setHttpResponseHeader(header);
        } else {
            HttpResponseHeader header = getResponseHeader(request, connection);
            builder.setMessage(msg);
            builder.setStatusCode(statusCode);
            builder.setBody(null);
            builder.setHttpResponseHeader(header);
            connection.disconnect();
        }
        return builder.build();
    }

    protected final void releaseBodyResource(HttpRequest request) {
        request.getRequestBody().releaseResource();
    }


    //    public HttpResponse performRequest(HttpRequest request) throws Throwable {
//        HttpResponse.Builder builder = new HttpResponse.Builder(request);
//        builder = performRequest(request, builder);
//        HttpResponse response = builder.build();
//        response = onAfterRunning(request, response);
//        return response;
//    }
//
//
//    protected HttpResponse.Builder performRequest(final HttpRequest request, final HttpResponse.Builder builder) throws Throwable{
//        checkNotNulls(request, builder);
//        HttpURLConnection connection = null;
//        try {
//            onLoading(request);
//            onPrepareRunning(request);
//            String url = getUrl(request);
//            URL addressUrl = new URL(url);
//            connection = (HttpURLConnection) addressUrl.openConnection();
//            connection.setRequestMethod(request.methodString());
//            connection.setInstanceFollowRedirects(true);
//            setTimeout(connection); // set connect timeout and read timeout
////            if (isMultipart(request)) {
////                request.getRequestHeader().remove(Headers.CONTENT_LENGTH);
////            }
////            onPrepareRunning(request);
//            writeRequestHeader(request, connection); // set http request header
//            onWriteRequestHeaderFinish(request);
//            writeRequestBody(request, connection); // send request body to server
//            onAfterWriteRequestBody(request);
//
//            connection.connect(); // connection to server
//
//            int statusCode = onReadResponseCode(request, connection.getResponseCode());
//            String msg = connection.getResponseMessage();
//            HttpResponseHeader responseHeader = onReadResponseHeader(request, readResponseHeaders(request, connection));
//            if (isRedirect(statusCode)) { //redirect to next url
//                int count = builder.increaseAndGetRedirectCount();
//                if (count > MAX_REDIRECT_COUNT) {
//                    //TODO
//                }
//
//                String forwardUrl = responseHeader.getForwardUrl();
//                if (ValueUtil.isEmpty(forwardUrl)) {
//                    //TODO
//                }
//                System.out.println(request.urlString() + " Code " + statusCode + ", redirect to " + forwardUrl);
//                request.setForwardUrl(forwardUrl);
//                RedirectedResponse redirectedResponse = new RedirectedResponse(statusCode, msg, forwardUrl, responseHeader);
//                redirectedResponse = onPrepareRedirect(request, redirectedResponse, count);//TODO, setCookie, Others
//                builder.addRedirectedResponse(redirectedResponse);
//                connection.disconnect();
//                return performRequest(request, builder); //redirect to next request
//            } else if (isSuccess(statusCode)) {
//                builder.setMessage(msg);
//                builder.setStatusCode(statusCode);
//                HttpResponseBody responseBody = readResponseBody(request, connection);
//                responseBody = onReadRequestBodyFinish(request, responseBody);
//                builder.setBody(responseBody);
//                builder.setHttpResponseHeader(responseHeader);
//                return builder;
//            } else { // failed
//                builder.setMessage(msg);
//                builder.setStatusCode(statusCode);
//                builder.setBody(null);
//                builder.setHttpResponseHeader(responseHeader);
//                connection.disconnect();
//                return builder;
//            }
//        } catch (MalformedURLException e) {
//            e.printStackTrace();
//            releaseBodyResource(request);
//            if (null != connection) {
//                connection.disconnect();
//            }
//            throw e;
//        } catch (IOException e) {
//            e.printStackTrace();
//            releaseBodyResource(request);
//            if (null != connection) {
//                connection.disconnect();
//            }
//            throw e;
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//            releaseBodyResource(request);
//            if (null != connection) {
//                connection.disconnect();
//            }
//            throw e;
//        }
//    }
//
//    protected final void releaseBodyResource(HttpRequest request) {
//        List<HttpRequestBody.EntityHolder> entityHolders = request.getRequestBody().getResources();
//        if (null == entityHolders || entityHolders.size() <= 0) return;
//        for (HttpRequestBody.EntityHolder entityHolder : entityHolders) {
//            try {
//                IOUtil.closeQuietly(entityHolder.resource.openStream());
//            } catch (IOException e) {
//                e.printStackTrace();
//            }
//        }
//    }
//
//    protected HttpResponseBody readResponseBody(final HttpRequest httpRequest, HttpURLConnection connection)
//            throws IOException {
//        final TransportProgress transportProgress = httpRequest.getTransportProgress();
//        final long contentLength = connection.getContentLengthLong();//TODO
//        InputStream inputStream = connection.getInputStream();
//        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(inputStream, connection) {
//            @Override
//            protected void onRead(long readCount) throws IOException {
//                System.out.println("Read count " + readCount);
//                onTransportDownProgress(httpRequest, readCount, contentLength);
//            }
//        };
//
//        HttpResponseBody responseBody = new HttpResponseBody(new StreamBinaryResource(inputStreamWrapper, contentLength));
//        return responseBody;
//    }
//
//    protected HttpResponseHeader readResponseHeaders(HttpRequest httpRequest, HttpURLConnection connection)
//            throws IOException {
//        connection.getResponseCode();
//        return new HttpResponseHeader(connection.getHeaderFields());
//    }
//
//
    public boolean isMultipart(HttpRequest httpRequest) {
        if (Method.POST != httpRequest.getMethod()) return false;
        List<HttpRequestBody.EntityHolder> entityHolders = httpRequest.getRequestBody().getResources();
        if (entityHolders.size() > 0) {
            System.out.println("url post request not need upload anything.");
            return true;
        }
        return false;
    }

    /**
     * Returns true if this response redirects to another resource.
     */
    public boolean isRedirect(int code) {
        switch (code) {
            case HTTP_PERM_REDIRECT:
            case HTTP_TEMP_REDIRECT:
            case HTTP_MULT_CHOICE:
            case HTTP_MOVED_PERM:
            case HTTP_MOVED_TEMP:
            case HTTP_SEE_OTHER:
                return true;
            default:
                return false;
        }
    }

    protected final boolean isSuccess(int code) {
        if (code >= 200 && code < 300) {
            return true;
        }
        return false;
    }

    /**
     * Return the url will be request.
     */
    protected String getUrl(HttpRequest request) {
        String url = request.getNextUrl();
        if (ValueUtil.isEmpty(url)) {
            url = request.getUrl();
        }
        return url;
    }
}
