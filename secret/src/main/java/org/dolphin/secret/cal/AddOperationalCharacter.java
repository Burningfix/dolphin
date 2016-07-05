package org.dolphin.secret.cal;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class AddOperationalCharacter implements OperationalCharacter {
    private final BigDecimal value;

    public AddOperationalCharacter(BigDecimal value) {
        this.value = value;
    }

    @Override
    public Type getType() {
        return Type.ADD;
    }

    @Override
    public BigDecimal getValue() {
        return null;
    }
}
