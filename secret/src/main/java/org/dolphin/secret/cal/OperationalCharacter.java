package org.dolphin.secret.cal;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public interface OperationalCharacter {
    public enum Type {
        EOF,
        NUMBER, // just a number
        LEFT_PARENTHESES,  // LEFT PARENTHESES,(
        RIGHT_PARENTHESES,  // RIGHT PARENTHESES, )
        MULTIPLY,
        DIVIDE,
        ADD,
        SUBTRACT,
    }

    public Type getType();

    public BigDecimal getValue();
}
