package org.dolphin.http;


import org.dolphin.lib.SecurityUtil;
import org.dolphin.lib.ValueUtil;
import org.dolphin.lib.binaryresource.BinaryResource;

import java.io.Closeable;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.net.URLEncoder;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Objects;
import java.util.Set;

import javax.annotation.Nullable;

import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkState;

/**
 * Created by dolphin on 2015/5/13.
 * The main http request.
 */
public class HttpRequest implements Cloneable, Closeable {
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

    /**
     * 存储的是经过编码后的参数。
     */
    private final Map<String, Object> params = new LinkedHashMap<String, Object>();


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

    /**
     * 当用户输入时，需要对参数进行编码。
     *
     * @param params
     * @return
     */
    public HttpRequest params(Map<String, ?> params) {
        if (null != params) {
            try {
                for (Map.Entry<String, ?> param : params.entrySet()) {
                    this.params.put(URLEncoder.encode(param.getKey(), "UTF-8"),
                            URLEncoder.encode(param.getValue().toString(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }
        return this;
    }

    /**
     * 这是一个对外公开的，其中每个参数都是encode过的, 会附带扩展的参数和签名，签名格式为sign=xxxxxxxxxxxx(sha1)
     *
     * @return
     */
    public Map<String, Object> getParams() {
        // 得到只包含host的url
        String url = HttpUtil.getHost(getUrl());
        if(ValueUtil.isEmpty(url)) {
            throw new IllegalArgumentException("url["+url+"] is Illegal!");
        }

        LinkedHashMap<String, Object> outParams = new LinkedHashMap<String, Object>();

        // 拷贝用户传入参数
        outParams.putAll(this.params);

        // 增加可选的扩展参数
        Map<String, String> extensionParams = HttpExtension.getExtensionParams();
        if (null != extensionParams) {
            try {
                for (Map.Entry<String, String> param : extensionParams.entrySet()) {
                    outParams.put(URLEncoder.encode(param.getKey(), "UTF-8"),
                            URLEncoder.encode(param.getValue().toString(), "UTF-8"));
                }
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
        }

        // 按照添加顺序生成
        url = HttpUtil.generateUrl(url, outParams, false);

        // 对url进行签名
        outParams.put("sign", SecurityUtil.sha1(url));

        return outParams;
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
            try {
                builder.append(connectorChar).append(URLDecoder.decode(key, "UTF-8")).append("=")
                        .append(URLDecoder.decode(parameters.get(key).toString(), "UTF-8"));
            } catch (UnsupportedEncodingException e) {
                e.printStackTrace();
            }
            connectorChar = "&";
        }
        return builder.toString();
    }

    /**
     * 每个request的唯一标识，用于
     * @return
     */
    public String uniqueIdentification() {
        if (getMethod() == Method.POST) {
            StringBuilder stringBuilder = new StringBuilder();
            stringBuilder.append('[')
                    .append(getUrl())
                    .append(']')
                    .append(System.nanoTime());
            return SecurityUtil.sha1(stringBuilder.toString());
        }

        if (getMethod() == Method.GET) {
            Map<String, ?> parameters = this.params;
            String connectorChar = "&";
            String url = this.url;
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
                try {
                    builder.append(connectorChar).append(URLDecoder.decode(key, "UTF-8")).append("=")
                            .append(URLDecoder.decode(parameters.get(key).toString(), "UTF-8"));
                } catch (UnsupportedEncodingException e) {
                    e.printStackTrace();
                }
                connectorChar = "&";
            }
            return SecurityUtil.sha1(builder.toString());
        }

        return SecurityUtil.sha1(String.valueOf(System.nanoTime()));
    }

    @Override
    public void close() throws IOException {
        // TODO: release current request's resource
    }
}
