package org.dolphin.http;

import com.hanyanan.http.TransportProgress;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.List;
import java.util.Map;
import java.util.Set;

import hyn.com.lib.IOUtil;
import hyn.com.lib.Preconditions;
import hyn.com.lib.ValueUtil;

import static java.net.HttpURLConnection.HTTP_MOVED_PERM;
import static java.net.HttpURLConnection.HTTP_MOVED_TEMP;
import static java.net.HttpURLConnection.HTTP_MULT_CHOICE;
import static java.net.HttpURLConnection.HTTP_SEE_OTHER;

/**
 * Created by hanyanan on 2015/5/22.
 */
public class HttpUrlLoader extends HttpLoader.BaseHttpLoader implements HttpLoader {


    @Override
    public HttpResponse performRequest(HttpRequest request) throws Throwable {
        HttpResponse.Builder builder = new HttpResponse.Builder(request);
        builder = performRequest(request, builder);
        HttpResponse response = builder.build();
        response = onAfterRunning(request, response);
        return response;
    }

    protected HttpResponse.Builder performRequest(final HttpRequest request, final HttpResponse.Builder builder) throws Throwable{
        Preconditions.checkNotNull(request);
        Preconditions.checkNotNull(builder);
        String url = getUrl(request);
        URL address_url = null;
        HttpURLConnection connection = null;
        try {
            onPrepareRunning(request);
            address_url = new URL(url);
            connection = (HttpURLConnection) address_url.openConnection();
            connection.setRequestMethod(request.methodString());
            connection.setInstanceFollowRedirects(true);
            setTimeout(connection); // set timeout for connection
            if (isMultipart(request)) {
                request.getRequestHeader().remove(Headers.CONTENT_LENGTH);
            }
            onPropertyInit(request);
            writeRequestHeader(request, connection); // set http request header
            onWriteRequestHeaderFinish(request);
            writeRequestBody(request, connection); // send request body to server
            onWriteRequestBodyFinish(request);

            connection.connect(); // connection to server

            int statusCode = onReadResponseCode(request, connection.getResponseCode());
            String msg = connection.getResponseMessage();
            HttpResponseHeader responseHeader = onReadResponseHeader(request, readResponseHeaders(request, connection));
            if (isRedirect(statusCode)) { //redirect to next url
                int count = builder.increaseAndGetRedirectCount();
                if (count > MAX_REDIRECT_COUNT) {
                    //TODO
                }

                String forwardUrl = responseHeader.getForwardUrl();
                if (ValueUtil.isEmpty(forwardUrl)) {
                    //TODO
                }
                System.out.println(request.urlString() + " Code " + statusCode + ", redirect to " + forwardUrl);
                request.setForwardUrl(forwardUrl);
                RedirectedResponse redirectedResponse = new RedirectedResponse(statusCode, msg, forwardUrl, responseHeader);
                redirectedResponse = onPrepareRedirect(request, redirectedResponse, count);//TODO, setCookie, Others
                builder.addRedirectedResponse(redirectedResponse);
                connection.disconnect();
                return performRequest(request, builder); //redirect to next request
            } else if (isSuccess(statusCode)) {
                builder.setMessage(msg);
                builder.setStatusCode(statusCode);
                HttpResponseBody responseBody = readResponseBody(request, connection);
                responseBody = onReadRequestBodyFinish(request, responseBody);
                builder.setBody(responseBody);
                builder.setHttpResponseHeader(responseHeader);
                return builder;
            } else { // failed
                builder.setMessage(msg);
                builder.setStatusCode(statusCode);
                builder.setBody(null);
                builder.setHttpResponseHeader(responseHeader);
                connection.disconnect();
                return builder;
            }
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
        } catch (InterruptedException e) {
            e.printStackTrace();
            releaseBodyResource(request);
            if (null != connection) {
                connection.disconnect();
            }
            throw e;
        }
    }

    protected final void releaseBodyResource(HttpRequest request) {
        List<HttpRequestBody.EntityHolder> entityHolders = request.getRequestBody().getResources();
        if (null == entityHolders || entityHolders.size() <= 0) return;
        for (HttpRequestBody.EntityHolder entityHolder : entityHolders) {
            try {
                IOUtil.closeQuietly(entityHolder.resource.openStream());
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    protected HttpResponseBody readResponseBody(final HttpRequest httpRequest, HttpURLConnection connection)
            throws IOException {
        final TransportProgress transportProgress = httpRequest.getTransportProgress();
        final long contentLength = connection.getContentLengthLong();//TODO
        InputStream inputStream = connection.getInputStream();
        InputStreamWrapper inputStreamWrapper = new InputStreamWrapper(inputStream, connection) {
            @Override
            protected void onRead(long readCount) throws IOException {
                System.out.println("Read count " + readCount);
                onTransportDownProgress(httpRequest, readCount, contentLength);
            }
        };

        HttpResponseBody responseBody = new HttpResponseBody(new StreamBinaryResource(inputStreamWrapper, contentLength));
        return responseBody;
    }

    protected HttpResponseHeader readResponseHeaders(HttpRequest httpRequest, HttpURLConnection connection)
            throws IOException {
        connection.getResponseCode();
        return new HttpResponseHeader(connection.getHeaderFields());
    }


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

    protected void writeRequestBody(HttpRequest request, URLConnection connection) throws IOException {
        //default not write content
    }

    /**
     * @param request
     */
    protected void writeRequestHeader(HttpRequest request, URLConnection connection) {
        HttpRequestHeader requestHeader = request.getRequestHeader();
        if (null == requestHeader) return;
        Map<String, String> headers = requestHeader.maps();
        Set<Map.Entry<String, String>> entrySet = headers.entrySet();
        if (null == entrySet) return;
        for (Map.Entry<String, String> entry : entrySet) {
            connection.setRequestProperty(entry.getKey(), entry.getValue());
        }
    }

    protected void setTimeout(URLConnection connection) {
        connection.setConnectTimeout(5000);
        connection.setReadTimeout(5000);
    }

    /**
     * Return the url will be request.
     */
    protected String getUrl(HttpRequest request) {
        String url = request.getForwardUrl();
        if (ValueUtil.isEmpty(url)) {
            url = request.getUrl();
        }
        return url;
    }
}
