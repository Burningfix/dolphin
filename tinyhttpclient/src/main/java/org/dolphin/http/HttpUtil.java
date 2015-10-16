package org.dolphin.http;


import org.dolphin.lib.DateUtils;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Map;
import java.util.Set;

/**
 * Created by dolphin on 2015/5/11.
 */
public class HttpUtil {
    public static final byte[] CRLF = {'\r', '\n'};
    public static final String DEFAULT_CHARSET = "utf-8";
    public static final String DEFAULT_ENCODING = "gzip";
    public static final int DEFAULT_TIMEOUT = 5000;//5s
    public static final String DEFAULT_USER_AGENT = "horizontal-version 0.0.1 :)";
    public static final String CONNECTOR = ";";
    public static final String BREAK = ":";

    /**
     * 标准的参考时间，一般是从服务器获取的标准时间，当从服务器获取标准的时间时，计算出服务器时间和当前本地时间的时间差，
     * 一般为(服务器时间-本地时间)
     */
    public static Long sStandardMillTimeDifference = null;

    /**
     * 设置当前服务器和本地的时间差，计算模式为服务器时间-本地时间
     *
     * @param serverTime 服务器时间
     */
    public static synchronized void setServerTime(long serverTime) {
        sStandardMillTimeDifference = Long.valueOf(serverTime - getCurrentTime());
    }

    /**
     * 获得服务器时间，如果与服务器时间同步过，则计算时间差即可，否则直接返回当前时间。
     *
     * @return 当前server时间
     */
    public static synchronized long getServerTime() {
        if (null != sStandardMillTimeDifference) {
            return sStandardMillTimeDifference.longValue() + System.currentTimeMillis();
        }
        return getCurrentTime();
    }

    public static long getCurrentTime() {
        return System.currentTimeMillis();
    }

    /**
     * Returns the next index in {@code input} at or after {@code pos} that
     * contains a character from {@code characters}. Returns the input length if
     * none of the requested characters can be found.
     */
    public static int skipUntil(String input, int pos, String characters) {
        for (; pos < input.length(); pos++) {
            if (characters.indexOf(input.charAt(pos)) != -1) {
                break;
            }
        }
        return pos;
    }

    /**
     * Returns the next non-whitespace character in {@code input} that is white
     * space. Result is undefined if input contains newline characters.
     */
    public static int skipWhitespace(String input, int pos) {
        for (; pos < input.length(); pos++) {
            char c = input.charAt(pos);
            if (c != ' ' && c != '\t') {
                break;
            }
        }
        return pos;
    }

    /**
     * Parse date in RFC1123 format, and return its value as epoch
     */
    public static long parseDateAsEpoch(String dateStr) {
        try {
            // Parse date in RFC1123 format if this header contains one
            return DateUtils.parseDate(dateStr).getTime();
        } catch (Exception e) {
            // Date in invalid format, fallback to 0
            return 0;
        }
    }

    /**
     * Retrieve a charset from headers
     *
     * @param headers        An {@link Map} of headers
     * @param defaultCharset Charset to return if none can be found
     * @return Returns the charset specified in the Content-Type of this header,
     * or the defaultCharset if none can be found.
     */
    public static String parseCharset(Map<String, String> headers, String defaultCharset) {
        String contentType = headers.get(Headers.CONTENT_TYPE);
        if (contentType != null) {
            String[] params = contentType.split(";");
            for (int i = 1; i < params.length; i++) {
                String[] pair = params[i].trim().split("=");
                if (pair.length == 2) {
                    if (pair[0].equals("charset")) {
                        return pair[1];
                    }
                }
            }
        }

        return defaultCharset;
    }

    /**
     * Returns {@code value} as a positive integer, or 0 if it is negative, or
     * {@code defaultValue} if it cannot be parsed.
     */
    public static int parseSeconds(String value, int defaultValue) {
        try {
            long seconds = Long.parseLong(value);
            if (seconds > Integer.MAX_VALUE) {
                return Integer.MAX_VALUE;
            } else if (seconds < 0) {
                return 0;
            } else {
                return (int) seconds;
            }
        } catch (NumberFormatException e) {
            return defaultValue;
        }
    }


    public static String generateUrl(String url, Map<String, ?> parameters) {
        if (parameters == null || url == null || parameters.isEmpty()) {
            return url;
        }
        String connectorChar = "&";
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
        try {
            for (String key : keySet) {
                builder.append(connectorChar).append(URLEncoder.encode(key, "UTF-8")).append("=")
                        .append(URLEncoder.encode(String.valueOf(parameters.get(key)), "UTF-8"));
                connectorChar = "&";
            }
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
        return builder.toString();
    }
}
