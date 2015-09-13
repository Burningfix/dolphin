package org.dolphin.http;

import static org.dolphin.lib.Preconditions.checkNotNull;
/**
 * Created by hanyanan on 2015/5/10.
 * The precondition for http request body.
 */
public class HttpPreconditions {
    public static void checkUrl(String url) {
        checkNotNull(url);
        // TODO
    }

    public static boolean invalidatesCache(String method) {
        return method.equals("POST")
                || method.equals("PATCH")
                || method.equals("PUT")
                || method.equals("DELETE");
    }

    public static boolean requiresRequestBody(String method) {
        return method.equals("POST")
                || method.equals("PUT")
                || method.equals("PATCH");
    }

    public static boolean permitsRequestBody(String method) {
        return requiresRequestBody(method)
                || method.equals("DELETE"); // Permitted as spec is ambiguous.
    }

    private HttpPreconditions() {
    }
}
