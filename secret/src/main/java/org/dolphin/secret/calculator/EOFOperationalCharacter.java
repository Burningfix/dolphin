package org.dolphin.secret.calculator;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class EOFOperationalCharacter implements OperationalCharacter {
    private final BigDecimal value;

    public EOFOperationalCharacter(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.EOF;
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return "EOF";
    }
}
