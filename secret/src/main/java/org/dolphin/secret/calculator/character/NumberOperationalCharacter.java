package org.dolphin.secret.calculator.character;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class NumberOperationalCharacter implements OperationalCharacter {
    private final BigDecimal value;

    public NumberOperationalCharacter(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.NUMBER;
    }

    @Override
    public BigDecimal getValue() {
        return this.value;
    }

    @Override
    public String toString() {
        return getValue().toString();
    }
}
