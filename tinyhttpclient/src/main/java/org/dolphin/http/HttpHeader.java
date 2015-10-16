package org.dolphin.http;

import com.google.common.collect.Maps;
import org.dolphin.lib.SecurityUtil;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;

import static org.dolphin.http.HttpUtil.BREAK;
import static org.dolphin.http.HttpUtil.CONNECTOR;
import static org.dolphin.http.HttpUtil.CRLF;
import static org.dolphin.lib.Preconditions.checkArgument;
import static org.dolphin.lib.Preconditions.checkNotNull;

/**
 * The header information for a http request. It mark the
 * cache controller/expire time/referer......
 * </pr>
 * Default Request header as follow:
 * Connection: keep-alive
 * Accept-Encoding: compress, gzip
 * Accept-Language: en,zh
 * Accept-Ranges: bytes
 * User-Agent: HANYANAN VERSION 0.0.1
 * Cache-Control: no-cache
 * Accept-Charset:
 * Accept: text/plain, text/html
 * Pragma: no-cache
 */

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
 * Cache-Control的max-age优先级高于Expires(至少对于Apache是这样的）,即如果定义了Cache-Control: max-age，
 * 则完全不需要加上Expries，因为根本没用。
 * Cache-Control在Apache中的设置为  Header set Cache-Control "max-age: 60" , Expires是相同的功能，不过参数是个绝对的日期，
 * 不是一个相对的值 Header set Expires "Thu, 15 Apr 2010 20:00:00 GMT"
 */
public class HttpHeader {
    protected static final Map<String, String> DEFAULT_HEADERS = new LinkedHashMap<String, String>();

    static {
        DEFAULT_HEADERS.put(Headers.CONNECTION.value(), "keep-alive");
        DEFAULT_HEADERS.put(Headers.ACCEPT_ENCODING.value(), "gzip, deflate");
        DEFAULT_HEADERS.put(Headers.ACCEPT_LANGUAGE.value(), "en,zh");
        DEFAULT_HEADERS.put(Headers.ACCEPT_RANGES.value(), "bytes");
        DEFAULT_HEADERS.put(Headers.USER_AGENT.value(), "TINY VERSION 0.0.1");
        DEFAULT_HEADERS.put(Headers.CACHE_CONTROL.value(), "no-cache");
        DEFAULT_HEADERS.put(Headers.ACCEPT_CHARSET.value(), "utf-8");
        DEFAULT_HEADERS.put(Headers.ACCEPT.value(), "*/*");
        DEFAULT_HEADERS.put(Headers.PRAGMA.value(), "no-cache");
    }

    protected final Map<String, String> headers = Maps.newLinkedHashMap();

    /**
     * The headers with high priority headers, it will override the {@link #headers}, it store the
     * manual operations.
     */
    protected final Map<String, String> priorHeaders = Maps.newHashMap();

    public HttpHeader(HttpHeader header) {
        if (null != header && null != header.headers) {
            this.headers.putAll(header.headers);
        }
    }

    public HttpHeader(Map<String, List<String>> headers) {
        if (null == headers || headers.isEmpty()) return;
        Set<Map.Entry<String, List<String>>> entrySet = headers.entrySet();
        if (null == entrySet || entrySet.isEmpty()) return;
        for (Map.Entry<String, List<String>> entry : entrySet) {
            if (null == entry || entry.getKey() == null || entry.getValue() == null
                    || entry.getValue().isEmpty()) continue;
            StringBuilder value = new StringBuilder();
            for (String val : entry.getValue()) {
                if (!SecurityUtil.isEmpty(value)) {
                    value.append(CONNECTOR).append(val);
                } else {
                    value.append(val);
                }
            }
            this.headers.put(entry.getKey().toLowerCase(Locale.ENGLISH), value.toString());
        }
    }

    /**
     * Add an header line containing a field name, a literal colon, and a value.
     */
    public HttpHeader add(String line) {
        int index = HttpUtil.skipUntil(line, 0, ":=");
        checkArgument(index > 0 && index < line.length(), "Unexpected header: " + line);
        return add(line.substring(0, index).trim(), line.substring(index + 1));
    }

    /**
     * Add a field with the specified value.
     */
    public HttpHeader add(String name, Object value) {
        checkNotNull(name, "name == null");
        checkNotNull(value, "value == null");
        checkNotNull(value.toString(), "value.toString() == null");
        name = name.toLowerCase(Locale.ENGLISH);
        if (name.length() == 0 || name.indexOf('\0') != -1
                || value.toString() == null || value.toString().indexOf('\0') != -1) {
            throw new IllegalArgumentException("Unexpected header: " + name + ": " + value);
        }

        String prev = headers.get(name);
        if (!SecurityUtil.isEmpty(prev.toString())) {
            headers.put(name, prev.toString() + CONNECTOR + value.toString());
        } else {
            headers.put(name, value.toString());
        }

        return this;
    }

    public HttpHeader remove(Object attr) {
        checkNotNull(attr);
        checkNotNull(attr.toString());
        headers.remove(attr.toString().toLowerCase(Locale.ENGLISH));
        return this;
    }

    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public HttpHeader put(String line) {
        int index = HttpUtil.skipUntil(line, 0, ":=");
        checkArgument(index > 0 && index < line.length(), "Unexpected header: " + line);
        return setHeadProperty(line.substring(0, index).trim(), line.substring(index + 1));
    }

    public Map<String, String> maps() {
        Map<String, String> res = Maps.newHashMap();
        res.putAll(headers);
        res.putAll(priorHeaders);
        return res;
    }


    /**
     * Set a field with the specified value. If the field is not found, it is
     * added. If the field is found, the existing values are replaced.
     */
    public HttpHeader setHeadProperty(String attr, Object value) {
        checkNotNull(attr, "attr == null");
        checkNotNull(value, "value == null");
        headers.put(attr.toLowerCase(Locale.ENGLISH), value.toString());
        return this;
    }

    public HttpHeader setPriorHeadProperty(String attr, Object value) {
        checkNotNull(attr, "attr == null");
        checkNotNull(value, "value == null");
        this.priorHeaders.put(attr.toLowerCase(Locale.ENGLISH), value.toString());
        return this;
    }

    public int size() {
        return headers.size() + priorHeaders.size();
    }

//    public String name(int index) {
//        Map<String, String> maps = maps();
//        if(maps.size() <= index) return null;
//        return maps.
//    }
//
//    public String value(int index) {
//
//    }

    public String value(Object key) {
        checkNotNull(key);
        String k = key.toString();
        checkNotNull(k);
        if (headers.containsKey(k)) {
            Object res = headers.get(k);
            if (null != res) return res.toString();
        }
        if (priorHeaders.containsKey(k)) {
            Object res = priorHeaders.get(k);
            if (null != res) return res.toString();
        }
        return null;
    }


    /**
     * Formate the current head to string mode.
     *
     * @return
     */
    public String string() {
        final Map<String, String> header = Maps.newHashMap(headers);
        header.putAll(priorHeaders);
        Set<Map.Entry<String, String>> entries = header.entrySet();
        if (null == entries || entries.size() <= 0) return null;
        StringBuilder stringBuilder = new StringBuilder();
        for (Map.Entry<String, String> entry : entries) {
            stringBuilder.append(entry.getKey()).append(BREAK).append(entry.getValue()).append(CRLF);
        }
        return stringBuilder.toString();
    }
}
