package org.dolphin.http;

import java.io.IOException;

/**
 * Created by hanyanan on 2015/5/9.
 */
public enum Protocol {
    /**
     * An obsolete plaintext framing that does not use persistent sockets by
     * default.
     */
    HTTP_1_0("http/1.0"),

    /**
     * A plaintext framing that includes persistent connections.
     *
     * <p>This version of OkHttp implements <aref="http://www.ietf.org/rfc/rfc2616.txt">RFC 2616</a>, and tracks
     * revisions to that spec.
     */
    HTTP_1_1("http/1.1");

    private final String protocol;

    Protocol(String protocol) {
        this.protocol = protocol;
    }

    /**
     * Returns the protocol identified by {@code protocol}.
     * @throws IOException if {@code protocol} is unknown.
     */
    public static Protocol get(String protocol) throws IOException {
        // Unroll the loop over values() to save an allocation.
        if (protocol.equals(HTTP_1_0.protocol)) return HTTP_1_0;
        if (protocol.equals(HTTP_1_1.protocol)) return HTTP_1_1;
        throw new IOException("Unexpected protocol: " + protocol);
    }

    /**
     * Returns the string used to identify this protocol for ALPN, like
     * "http/1.1", or "http/2.0".
     */
    @Override public String toString() {
        return protocol;
    }
}
