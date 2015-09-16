package org.dolphin.http;


import org.dolphin.lib.DateUtils;
import org.dolphin.lib.ValueUtil;

import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.dolphin.http.HttpUtil.DEFAULT_CHARSET;

/**
 * Created by dolphin on 2015/5/9.
 */
public class HttpResponseHeader extends BaseHeader {
    private MimeType mimeType;
//    private CacheControl cacheControl;
    private Range range = null;
    public HttpResponseHeader(Map<String, List<String>> headers) {
        super(headers);
    }

    public MimeType getMimeType() {
        if (null == mimeType) {
            String contentType = getContentType();
            if (!ValueUtil.isEmpty(contentType)) {
                mimeType = MimeType.crateFromContentType(contentType);
            } else {
                mimeType = MimeType.defaultMimeType();
            }
        }
        return mimeType;
    }


    public String getCookie() {
        return value(Headers.SET_COOKIE.value());
    }

    //Content-Range: bytes 21010-47021/47022
    public Range getRange() {
        if (null != this.range) {
            return this.range;
        }
        String range = value(Headers.CONTENT_RANGE.value());
        if (null == range) return null;
        Pattern pattern = Pattern.compile("(\\d+)-(\\d+)/(\\d+)");
        Matcher m = pattern.matcher(range);
        if (m.find()) {
            String start = m.group(1);
            String end = m.group(2);
            String full = m.group(3);
            long s = ValueUtil.parseLong(start, 0);
            long e = ValueUtil.parseLong(end, -1);
            long f = ValueUtil.parseLong(full, -1);
            this.range = new Range(s, e, f);
        }
        return this.range;
    }

    public boolean isSupportCache() {
        // TODO
        return false;
    }

    public String getCharset() {
        MimeType mimeType = getMimeType();
        if (null == mimeType.getCharset()) return DEFAULT_CHARSET;
        return mimeType.getCharset();
    }

    /**
     * Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。
     * 例如：Content-Type: text/html; charset='gb2312'
     */
    public String getContentType() {
        return value(Headers.CONTENT_TYPE);
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
    public String getETag() {
        return value(Headers.E_TAG);
    }

//    public CacheControl getCacheControl(){
//        if(null == cacheControl) {
//            cacheControl = CacheControl.parse(this);
//        }
//        return cacheControl;
//    }
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
     * max-age：（本响应包含的对象的过期时间,以秒计算）
     * <p/>
     * ALL: no-store（不允许缓存）
     * Currently just support no-cache and max-age
     * Cache-Control的max-age优先级高于Expires(至少对于Apache是这样的）,即如果定义了Cache-Control: max-age，
     * 则完全不需要加上Expries，因为根本没用。
     * Cache-Control在Apache中的设置为  Header set Cache-Control "max-age: 60" , Expires是相同的功能，不过参数是个绝对的日期，
     * 不是一个相对的值 Header set Expires "Thu, 15 Apr 2010 20:00:00 GMT"
     * Cache-Control会覆盖Expires字段。
     * <p/>
     * cache-request-directive =
     * "no-cache"
     * | "no-store"
     * no-cache表示必须先与服务器确认返回的响应是否被更改，然后才能使用该响应来满足后续对同一个网址的请求。
     * 因此，如果存在合适的验证令牌 (ETag)，no-cache 会发起往返通信来验证缓存的响应，如果资源未被更改，可以避免下载。（即no-cache不会有请求的实体）
     * 相比之下，no-store更加简单，直接禁止浏览器和所有中继缓存存储返回的任何版本的响应 - 例如：一个包含个人隐私数据或银行数据的响应。
     * 每次用户请求该资源时，都会向服务器发送一个请求，每次都会下载完整的响应。<p/>
     *
     * | "max-age" "=" delta-seconds
     * | "max-stale" [ "=" delta-seconds ]
     * | "min-fresh" "=" delta-seconds
     * | "no-transform"
     * | "only-if-cached"
     * | cache-extension
     * cache-response-directive =
     * "public"
     * | "private" [ "=" <"> 1#field-name <"> ]
     * | "no-cache" [ "=" <"> 1#field-name <"> ]
     * 如果响应被标记为public，即使有关联的 HTTP 认证，甚至响应状态码无法正常缓存，响应也可以被缓存。
     * 大多数情况下，public不是必须的，因为明确的缓存信息（例如max-age）已表示 响应可以被缓存。
     * 相比之下，浏览器可以缓存private响应，但是通常只为单个用户缓存，因此，不允许任何中继缓存对其进行缓存
     * - 例如，用户浏览器可以缓存包含用户私人信息的 HTML 网页，但是 CDN 不能缓存。
     * | "no-store"
     * | "no-transform"
     * | "must-revalidate"
     * | "proxy-revalidate"
     * | "max-age" "=" delta-seconds 该指令指定从当前请求开始，允许获取的响应被重用的最长时间（单位为秒） - 例如：max-age=60表示响应可以再缓存和重用 60 秒。
     * | "s-maxage" "=" delta-seconds
     * | cache-extension
     *
     * @return
     */
    //Cache-Control: max-age=30
    public long getExpireTime() {
        //TODO
//        CacheControl cacheControl = getCacheControl();
//        if(null != cacheControl) {
//            int maxAge = cacheControl.maxAgeSeconds();
//            maxAge = maxAge<=0?cacheControl.sMaxAgeSeconds():maxAge;
//            if(maxAge >= 0){
//                long serverDate = getServerDate();
//                serverDate = serverDate<=0? TimeUtils.getCurrentWallClockTime():serverDate;
//                return serverDate + maxAge;
//            }
//        }
        String expire = value(Headers.EXPIRES);
        if(ValueUtil.isEmpty(expire)) return -1;
        return HttpUtil.parseDateAsEpoch(expire);
    }

    public long getServerDate() {
        String serverDate = value(Headers.DATE);
        if (ValueUtil.isEmpty(serverDate)) return -1;
        Date date = DateUtils.parseDate(serverDate);
        if (null == date) return -1;
        return date.getTime();
    }

    public long getLastModified() {
        String serverDate = value(Headers.LAST_MODIFIED);
        if (ValueUtil.isEmpty(serverDate)) return -1;
        Date date = DateUtils.parseDate(serverDate);
        if (null == date) return -1;
        return date.getTime();
    }

    /**
     * Return the origin server suggesting filename
     * The server may be provide a default file name for current resource, most of time it will be return
     * {@code null}, So client cannot depend on this value.
     * </pr>
     * The Content-Disposition identify the default file name value in http headers which come from server. ie.
     * <b>Content-Disposition: attachment; filename="fname.ext"</b>. it will return the "fname.ext" as the default download
     * file name.
     * </pr>
     */
    public String getDisposition() {
        String contentDisposition = value(Headers.CONTENT_DISPOSITION);
        if (null == contentDisposition) return null;
        Pattern pattern = Pattern.compile("filename=[^\\w]?([^\"]+)[^\\w]?&");
        Matcher m = pattern.matcher(contentDisposition);
        if (m.find()) {
            String fileName = m.group(0);
            return fileName;
        }
        return null;
    }

    public String getForwardUrl() {
        return value(Headers.LOCATION);
    }
}
