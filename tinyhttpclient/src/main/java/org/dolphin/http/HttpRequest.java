package org.dolphin.http;


import org.dolphin.lib.binaryresource.BinaryResource;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nullable;

import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkState;

/**
 * Created by dolphin on 2015/5/13.
 * The main http request.
 */
public class HttpRequest implements Cloneable {
    /**
     * http request body, it's a
     */
    private HttpRequestBody requestBody;

    /**
     * Http request header.
     */
    private HttpRequestHeader requestHeader;

    /**
     * The raw url need to rquest.
     */
    private final String url;

    //The redirect url
    private String forwardUrl;

    /**
     * Method such as Get or Post, @see{Method}
     */
    private final Method method;

    /**
     * Current requesr protocaol, such as Http 1.0 or 1.1
     */
    private final Protocol protocol;

    /**
     * A monitor to record the traffic.
     */
    private final TrafficRecorder trafficStatus;

    /**
     * The tag user for caller identify the request.
     */
    private Object tag;
    /**  */
    private final Map<String, Object> params = new HashMap<String, Object>();


    public HttpRequest(String url, Method method, Protocol protocol) {
        this.url = url;
        this.method = method;
        this.protocol = protocol;
        this.requestBody = new HttpRequestBody();
        this.requestHeader = new HttpRequestHeader();
        this.trafficStatus = new TrafficRecorder.TrafficRecorderImpl();
    }

    /**
     * Clone a nearly totally same request from current, just clone request param/body/param. do not copy any other
     * attributes.
     *
     * @return
     */
    public HttpRequest clone() {
        HttpRequest res = new HttpRequest(this.getUrl(), this.method, this.protocol);
        res.setRequestHeader(this.getRequestHeader().clone());
        res.params.putAll(this.params);
        res.setHttpRequestBody(this.getRequestBody());

        return res;
    }

    public HttpRequest(String url, Method method) {
        this(url, method, Protocol.HTTP_1_1);
    }

    public HttpRequest(String url) {
        this(url, Method.GET, Protocol.HTTP_1_1);
    }

//    public HttpRequest setTransportProgress(TransportProgress transportProgress) {
//        this.transportProgress = transportProgress;
//        return this;
//    }
//
//    public TransportProgress getTransportProgress() {
//        return transportProgress;
//    }

    public HttpRequest setForwardUrl(String url) {
        this.forwardUrl = url;
        return this;
    }

//    public HttpFingerPrint getFingerPrint(){
//        return fingerPrint;
//    }

//    public String getFingerPrint() {
//        if (null == fingerPrint) {
//            return ValueUtil.md5(getUrl());
//        }
//        return fingerPrint;
//    }

    public String getNextUrl() {
        return forwardUrl;
    }

    @Nullable
    public HttpRequestBody getRequestBody() {
        if (HttpPreconditions.permitsRequestBody(method.toString())) {
            return requestBody;
        }
        return null;
    }

    public HttpRequestHeader getRequestHeader() {
        return requestHeader;
    }

    public void setRequestHeader(HttpRequestHeader requestHeader) {
        checkNotNull(requestHeader);
        this.requestHeader = requestHeader;
    }

    final public String getUrl() {
        return url;
    }

    final public Method getMethod() {
        return method;
    }

    public String methodString() {
        return method.toString();
    }

    public final TrafficRecorder getTrafficStatus() {
        return trafficStatus;
    }

    public final void setTag(Object tag) {
        this.tag = tag;
    }

    public final Object getTag() {
        return tag;
    }

    public HttpRequest addBodyEntity(String param, BinaryResource resource) {
        //TODO

        return this;
    }

    public HttpRequest setHttpRequestBody(HttpRequestBody httpRequestBody) {
        checkState(requestBody != null && !requestBody.hasContent(), "The body not empty!");
        this.requestBody = httpRequestBody;
        return this;
    }

    public HttpRequest setCookie(String cookie) {
        getRequestHeader().setRequestCookie(cookie);
        return this;
    }

    public HttpRequest range(long start, long count) {
        getRequestHeader().setRequestRange(start, count);
        return this;
    }

    public HttpRequest params(Map<String, ?> params) {
        if (null != params) {
            this.params.putAll(params);
        }
        return this;
    }

    public Map<String, Object> getParams() {
        return Collections.unmodifiableMap(params);
    }


    public HttpRequest setRequestSupportCache(final boolean supportCache) {
        getRequestHeader().setRequestSupportCache(supportCache);
        return this;
    }

    public HttpRequest setReferer(String referer) {
        getRequestHeader().setReferer(referer);
        return this;
    }

    public HttpRequest setAuthorization(String name, String passwd) {
        getRequestHeader().setAuthorization(name, passwd);
        return this;
    }

    public HttpRequest setETag(String eTag) {
        getRequestHeader().setETag(eTag);
        return this;
    }

    public HttpRequest setLastModifiedTime(long time) {
        getRequestHeader().setLastModifiedTime(time);
        return this;
    }

    public HttpRequest setHeadProperty(String key, String value) {
        checkNotNull(key);
        checkNotNull(value);
        getRequestHeader().setHeadProperty(key, value);
        return this;
    }

    public String urlString() {
        //TODO
        return this.getUrl();
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("[").append(getMethod().toString())
                .append(' ')
                .append(getLogInfo(this))
                .append(']');
        return sb.toString();
    }

    public static String getLogInfo(HttpRequest httpRequest) {
        Map<String, ?> parameters = httpRequest.getParams();
        if (parameters == null) {
            return httpRequest.getUrl();
        }
        String connectorChar = "&";
        String url = httpRequest.getUrl();
        StringBuilder builder = new StringBuilder(url);
        if (url.contains("?")) {
            if (!url.endsWith("?")) {
                connectorChar = "&";
            } else {
                connectorChar = "";
            }
        } else {
            connectorChar = "?";
        }
        Set<String> keySet = parameters.keySet();
        for (String key : keySet) {
            builder.append(connectorChar).append(key).append("=")
                    .append(parameters.get(key));
            connectorChar = "&";
        }
        return builder.toString();
    }

    public String uniqueIdentification() {
        if(getMethod() == Method.POST){
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('[')
                    .append(getUrl())
                    .append(']')
                    .append(System.nanoTime());
            return stringBuilder.toString();
        }


    }
}
