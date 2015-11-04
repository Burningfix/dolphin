package org.dolphin.job.http;

import org.dolphin.http.HttpRequest;
import org.dolphin.http.HttpRequestBody;
import org.dolphin.http.Method;
import org.dolphin.http.Protocol;
import org.dolphin.job.tuple.TwoTuple;
import org.dolphin.lib.binaryresource.BinaryResource;

import java.util.Iterator;
import java.util.Map;

/**
 * Created by hanyanan on 2015/10/23.
 */
public class HttpJobs {
    public static HttpRequest create(String url, Method method, Protocol protocol) {
        HttpRequest httpRequest = new HttpRequest(url, method, protocol);
        return httpRequest;
    }

    public static HttpRequest create(String url) {
        return create(url, Method.GET, Protocol.HTTP_1_1);
    }

    public static HttpRequest create(String url, Method method) {
        return create(url, method, Protocol.HTTP_1_1);
    }

    /**
     * @param url    请求的url
     * @param params 请求的参数, 未编码
     */
    public static HttpRequest createGetRequest(String url, Map<String, String> params) {
        HttpRequest request = create(url, Method.GET, Protocol.HTTP_1_1);
        request.params(params);
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param offset 请求的起始位置
     * @param end    请求的终止位置，如果到结束，则设置未-1
     * @return
     */
    public static HttpRequest createGetRequest(String url, Map<String, String> params, long offset, long end) {
        HttpRequest request = create(url, Method.GET, Protocol.HTTP_1_1);
        request.params(params);
        request.range(offset, end);
        return request;
    }

    /**
     * @param url    请求的url
     * @param offset 请求的起始位置
     * @param end    请求的终止位置，如果到结束，则设置未-1
     * @return
     */
    public static HttpRequest createGetRequest(String url, long offset, long end) {
        HttpRequest request = create(url, Method.GET, Protocol.HTTP_1_1);
        request.range(offset, end);
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 请求的参数, 未编码
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.params(params);
        return request;
    }

    /**
     * @param url 请求的url
     */
    public static HttpRequest createPostRequest(String url) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param offset 请求的起始位置
     * @param end    请求的终止位置，如果到结束，则设置未-1
     * @return
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params, long offset, long end) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.params(params);
        request.range(offset, end);
        return request;
    }

    /**
     * @param url    请求的url
     * @param offset 请求的起始位置
     * @param end    请求的终止位置，如果到结束，则设置未-1
     * @return
     */
    public static HttpRequest createPostRequest(String url, long offset, long end) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.range(offset, end);
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param body   请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params, HttpRequestBody body) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        if (null != params && params.size() > 0) {
            request.params(params);
        }
        request.setHttpRequestBody(body);
        return request;
    }

    /**
     * @param url  请求的url
     * @param body 请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, HttpRequestBody body) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.setHttpRequestBody(body);
        return request;
    }

    /**
     * @param url  请求的url
     * @param body 请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, Iterator<TwoTuple<String, BinaryResource>> body) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        if (null != body) {
            while (body.hasNext()) {
                TwoTuple<String, BinaryResource> resource = body.next();
                if (null == resource) {
                    continue;
                }
                request.addRequestBody(resource.value1, resource.value2);
            }
        }
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param body   请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params,
                                                Iterator<TwoTuple<String, BinaryResource>> body) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.params(params);
        if (null != body) {
            while (body.hasNext()) {
                TwoTuple<String, BinaryResource> resource = body.next();
                if (null == resource) {
                    continue;
                }
                request.addRequestBody(resource.value1, resource.value2);
            }
        }
        return request;
    }


    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param body   请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params,
                                                Iterator<TwoTuple<String, BinaryResource>> body,
                                                long start, long end) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.params(params);
        request.range(start, end);
        if (null != body) {
            while (body.hasNext()) {
                TwoTuple<String, BinaryResource> resource = body.next();
                if (null == resource) {
                    continue;
                }
                request.addRequestBody(resource.value1, resource.value2);
            }
        }
        return request;
    }

    /**
     * @param url    请求的url
     * @param params 未编码的请求参数
     * @param body   请求的主题，需要上传到server
     * @return
     */
    public static HttpRequest createPostRequest(String url, Map<String, String> params, HttpRequestBody body,
                                                long start, long end) {
        HttpRequest request = create(url, Method.POST, Protocol.HTTP_1_1);
        request.params(params);
        request.range(start, end);
        request.setHttpRequestBody(body);
        return request;
    }
}
