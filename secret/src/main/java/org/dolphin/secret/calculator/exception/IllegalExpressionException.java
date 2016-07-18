package org.dolphin.secret.calculator.exception;

/**
 * Created by yananh on 2016/7/17.
 */
public class IllegalExpressionException extends RuntimeException {
    public IllegalExpressionException() {
        super();
    }

    public IllegalExpressionException(String detailMessage) {
        super(detailMessage);
    }

    public IllegalExpressionException(String detailMessage, Throwable throwable) {
        super(detailMessage, throwable);
    }

    public IllegalExpressionException(Throwable throwable) {
        super(throwable);
    }
}
