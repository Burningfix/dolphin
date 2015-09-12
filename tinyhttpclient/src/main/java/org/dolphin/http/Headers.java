package org.dolphin.http;

import java.util.Locale;

/**
 * Created by hanyanan on 2015/5/10.
 * https://greenbytes.de/tech/webdav/draft-ietf-httpbis-p5-range-latest.html
 * http://stackoverflow.com/questions/18315787/http-1-1-response-to-multiple-range
 */
public enum Headers {
    /**
     * Some demo
     * HTTP/1.1 206 Partial Content
     * Date: Tue, 14 Nov 1995 06:25:24 GMT
     * Last-Modified: Tue, 14 July 04:58:08 GMT
     * Content-Length: 2331785
     * Content-Type: multipart/byteranges; boundary=THIS_STRING_SEPARATES
     * <p/>
     * --THIS_STRING_SEPARATES
     * Content-Type: video/example
     * Content-Range: exampleunit 1.2-4.3/25
     * <p/>
     * ...the first range...
     * --THIS_STRING_SEPARATES
     * Content-Type: video/example
     * Content-Range: exampleunit 11.2-14.3/25
     * <p/>
     * ...the second range
     * --THIS_STRING_SEPARATES--
     */


    /* *
     *   请求头字段                         说明	                       响应头字段
     *      Accept	               告知服务器发送何种媒体类型	             Content-Type
     * Accept-Language	             告知服务器发送何种语言	                 Content-Language
     * Accept-Charset	            告知服务器发送何种字符集	             Content-Type
     * Accept-Encoding	            告知服务器采用何种压缩方式	             Content-Encoding
     * */


    /**
     * 在HTTP中，与字符集和字符编码相关的消息头是Accept-Charset/Content-Type，另外主区区分Accept-Charset/Accept-Encoding/Accept-Language/Content-Type/Content-Encoding/Content-Language：
     * <p/>
     * Accept-Charset：浏览器申明自己接收的字符集，这就是本文前面介绍的各种字符集和字符编码，如gb2312，utf-8（通常我们说Charset包括了相应的字符编码方案）；
     * <p/>
     * Accept-Encoding：浏览器申明自己接收的编码方法，通常指定压缩方法，是否支持压缩，支持什么压缩方法（gzip，deflate），（注意：这不是只字符编码）；
     * <p/>
     * Accept-Language：浏览器申明自己接收的语言。语言跟字符集的区别：中文是语言，中文有多种字符集，比如big5，gb2312，gbk等等；
     * <p/>
     * Content-Type：WEB服务器告诉浏览器自己响应的对象的类型和字符集。例如：Content-Type: text/html; charset='gb2312'
     * <p/>
     * Content-Encoding：WEB服务器表明自己使用了什么压缩方法（gzip，deflate）压缩响应中的对象。例如：Content-Encoding：gzip
     * <p/>
     * Content-Language：WEB服务器告诉浏览器自己响应的对象的语言。
     */

    //common partials
    CONNECTION("Connection"),//Connection: close
    CACHE_CONTROL("Cache-Control"),//Cache-Control: no-cache
    CONTENT_TYPE("Content-Type"),//Content-Type: application/x-www-form-urlencoded//Content-Type: text/html; charset=utf-8
    PRAGMA("Pragma"),//Pragma: no-cache
    VIA("Via"),//Via: 1.0 fred, 1.1 nowhere.com (Apache/1.1)
    WARN("Warn"),//Warn: 199 Miscellaneous warning


    //client request partials
    ACCEPT("Accept"),//Accept: text/plain, text/html
    ACCEPT_CHARSET("Accept-Charset"),//Accept-Charset: iso-8859-5, unicode-1-1;q=0.8
    ACCEPT_ENCODING("Accept-Encoding"),//Accept-Encoding: compress, gzip
    ACCEPT_LANGUAGE("Accept-Language"),//Accept-Language: en,zh
    ACCEPT_RANGES("Accept-Ranges"),//Accept-Ranges: bytes
    RANGE("Range"),//Range: bytes=500-999
    AUTHORIZATION("Authorization"),//Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    COOKIE("Cookie"),//Cookie: $Version=1; Skin=new;
    DATE("Date"),//Date: Tue, 15 Nov 2010 08:12:31 GMT
    FROM("From"),//From: user@email.com
    HOST("Host"),//Host: www.zcmhi.com
    IF_MATCH("If-Match"),//If-Match: 737060cd8c284d8af7ad3082f209582d
    IF_MODIFIED_SINCE("If-Modified-Since"),//If-Modified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    IF_NONE_MATCH("If-None-Match"),//If-None-Match: 737060cd8c284d8af7ad3082f209582d
    IF_RANGE("If-Range"),//If-Range: 737060cd8c284d8af7ad3082f209582d
    IF_UNMODIFIED_SINCE("If-Unmodified-Since"),//If-Unmodified-Since: Sat, 29 Oct 2010 19:43:31 GMT
    MAX_FORWARDS("Max-Forwards"),//Max-Forwards: 10
    PROXY_AUTHORIZATION("Proxy-Authorization"),//Proxy-Authorization: Basic QWxhZGRpbjpvcGVuIHNlc2FtZQ==
    REFERER("Referer"),//Referer: http://www.zcmhi.com/archives/71.html
    USER_AGENT("User-Agent"),//User-Agent: Mozilla/5.0 (Linux; X11)
    UPGRADE("Upgrade"),//Upgrade: HTTP/2.0, SHTTP/1.3, IRC/6.9, RTA/x11

    //server response partials
    CONTENT_ENCODING("Content-Encoding"),//可以参考的值为：gzip,compress,deflate和identity。
    AGE("Age"),//Age: 12
    ALLOW("Allow"),//Allow: GET, HEAD
    //http://stackoverflow.com/questions/18315787/http-1-1-response-to-multiple-range
    CONTENT_LENGTH("Content-Length"),//Content-Length: 348, transfer-length of message body.
    CONTENT_LOCATION("Content-Location"),//Content-Location: /index.htm
    LOCATION("Location"),//Location: http://www.zcmhi.com/archives/94.html
    CONTENT_MD5("Content-MD5"),//Content-MD5: Q2hlY2sgSW50ZWdyaXR5IQ==
    CONTENT_RANGE("Content-Range"),//Content-Range: bytes 21010-47021/47022
    E_TAG("ETag"),//ETag: 737060cd8c284d8af7ad3082f209582d
    EXPIRES("Expires"),//Expires: Thu, 01 Dec 2010 16:00:00 GMT
    LAST_MODIFIED("Last-Modified"),//Last-Modified: Tue, 15 Nov 2010 12:45:26 GMT
    REFRESH("Refresh"),//Refresh: 5; url=http://www.zcmhi.com/archives/94.html
    RETRY_AFTER("Retry-After"),//Retry-After: 120
    SERVER("Server"),//Server: Apache/1.3.27 (Unix) (Red-Hat/Linux)
    SET_COOKIE("Set-Cookie"),//Set-Cookie: UserID=JohnDoe; Max-Age=3600; Version=1
    TRAILER("Trailer"),//Trailer: Max-Forwards
    TRANSFER_ENCODING("Transfer-Encoding"),//Transfer-Encoding:chunked, 有效的值为：Trunked和Identity.
    VARY("Vary"),//Vary: *
    WWW_AUTHENTICATE("WWW-Authenticate"),//WWW-Authenticate: Basic
    CONTENT_DISPOSITION("Content-Disposition");

    private final String header;

    Headers(String header) {
        this.header = header.toLowerCase(Locale.ENGLISH);
    }

    @Override
    public String toString() {
        return header;
    }

    public String value() {
        return header;
    }
}
