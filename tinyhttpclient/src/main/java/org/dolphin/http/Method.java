package org.dolphin.http;

/**
 * Created by hanyanan on 2015/5/13.
 * Just support GET and POST method, the other method will coming soon.
 */
public enum Method {
    GET("GET"),
    POST("POST");
//    PUT("PUT"),
//    OPTIONS("OPTIONS"),
//    TRACE("TRACE"),
//    DELETE("DELETE"),
//    PATCH("PATCH");

    private final String method;

    Method(String method) {
        this.method = method;
    }

    @Override public String toString() {
        return method;
    }
}
