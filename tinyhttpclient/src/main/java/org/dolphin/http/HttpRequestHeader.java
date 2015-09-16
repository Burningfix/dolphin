package org.dolphin.http;


import org.dolphin.lib.DateUtils;

import java.util.Date;

/**
 * Created by dolphin on 2015/5/9.
 */
public class HttpRequestHeader extends BaseHeader {
    MimeType mimeType;
    public HttpRequestHeader(BaseHeader header) {
        super(header);
        this.headers.putAll(DEFAULT_HEADERS);
    }

    public HttpRequestHeader() {
        this(null);
    }

    public HttpRequestHeader setMimeType(MimeType mimeType){
        this.mimeType = mimeType;
        return this;
    }

    public MimeType getMimeType(){
        if(null == mimeType) {
            mimeType = MimeType.defaultMimeType();
        }
        return mimeType;
    }

    public HttpRequestHeader setRequestCookie(String cookie) {
        setHeadProperty(Headers.COOKIE.value(), cookie);
        return this;
    }

    public HttpRequestHeader setRequestRange(long start, long count) {
        setHeadProperty(Headers.ACCEPT_RANGES.value(), "bytes");
        setHeadProperty(Headers.RANGE.value(), "bytes=" + start + "-" + (start + count));
        return this;
    }

    /**
     * Cache-Control：
     * 请求：
     * no-cache（不要缓存的实体，要求现在从WEB服务器去取）
     * max-age：（只接受 Age 值小于 max-age 值，并且没有过期的对象）
     * max-stale：（可以接受过去的对象，但是过期时间必须小于 max-stale 值）
     * min-fresh：（接受其新鲜生命期大于其当前 Age 跟 min-fresh 值之和的缓存对象）
     * 响应：
     * public(可以用 Cached 内容回应任何用户)
     * private（只能用缓存内容回应先前请求该内容的那个用户）
     * no-cache（可以缓存，但是只有在跟WEB服务器验证了其有效后，才能返回给客户端）
     * max-age：（本响应包含的对象的过期时间）
     * <p/>
     * ALL: no-store（不允许缓存）
     * Currently just support no-cache and max-age
     * @param supportCache
     * @return
     */
    public HttpRequestHeader setRequestSupportCache(final boolean supportCache){
        if(!supportCache) {
            setHeadProperty(Headers.CACHE_CONTROL.value(), "no-cache");
        } else {
            setHeadProperty(Headers.CACHE_CONTROL.value(), "public");
            setHeadProperty(Headers.PRAGMA.value(), "public");
        }
        return this;
    }

    /**
     * Set request charset
     */
    public HttpRequestHeader setRequestCharset(String charset) {
        setHeadProperty(Headers.ACCEPT_CHARSET.value(), charset);
        return this;
    }

    /**
     * Set Referer property for passing server's Hotlinking checking.
     */
    public HttpRequestHeader setReferer(String referer) {
        setHeadProperty(Headers.REFERER.value(), referer);
        return this;
    }

    /**
     * TODO
     */
    public HttpRequestHeader setAuthorization(String name, String passwd) {
        // TODO
        return this;
    }

    /**
     * Server response :
     * Etag    "427fe7b6442f2096dff4f92339305444"
     * Last-Modified   Fri, 04 Sep 2009 05:55:43 GMT
     * Client send request
     * If-None-Match   "427fe7b6442f2096dff4f92339305444"
     * If-Modified-Since   Fri, 04 Sep 2009 05:55:43 GMT
     *
     * It's recommand to send both
     */
    public HttpRequestHeader setETag(String eTag){
        setHeadProperty(Headers.IF_NONE_MATCH.value(), eTag);
        return this;
    }

    //send request with head {If-Modified-Since   Fri, 04 Sep 2009 05:55:43 GMT}
    public HttpRequestHeader setLastModifiedTime(long time) {
        Date date = new Date(time);
        setHeadProperty(Headers.IF_MODIFIED_SINCE.value(), DateUtils.formatDate(date));
        return this;
    }

    public HttpRequestHeader clone(){
        HttpRequestHeader res = new HttpRequestHeader(this);
        res.mimeType = this.mimeType;
        return res;
    }
}
