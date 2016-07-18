package org.dolphin.secret.calculator.character;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class EOFOperationalCharacter implements OperationalCharacter {

    public EOFOperationalCharacter() {

    }

    @Override
    public Type getType() {
        return Type.EOF;
    }

    @Override
    public BigDecimal getValue() {
        throw new IllegalAccessError();
    }

    @Override
    public String toString() {
        return "EOF";
    }
}
