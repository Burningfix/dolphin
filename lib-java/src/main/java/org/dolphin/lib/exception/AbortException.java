package org.dolphin.lib.exception;

/**
 * Created by hanyanan on 2015/9/16.
 */
public class AbortException extends RuntimeException {
    public AbortException() {
        super();
    }

    public AbortException(String message) {
        super(message);
    }

    public AbortException(String message, Throwable cause) {
        super(message, cause);
    }

    public AbortException(Throwable cause) {
        super(cause);
    }


    protected AbortException(String message, Throwable cause,
                               boolean enableSuppression,
                               boolean writableStackTrace) {
        super(message, cause, enableSuppression, writableStackTrace);
    }
}
