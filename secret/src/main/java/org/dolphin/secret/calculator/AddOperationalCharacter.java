package org.dolphin.secret.calculator;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class AddOperationalCharacter implements OperationalCharacter {
    private final OperationalCharacter operationalCharacter;

    public AddOperationalCharacter(OperationalCharacter operationalCharacter) {
        this.operationalCharacter = operationalCharacter;
    }

    @Override
    public Type getType() {
        return Type.ADD;
    }

    @Override
    public BigDecimal getValue() {
        return operationalCharacter.getValue();
    }

    @Override
    public String toString() {
        return "Add " + this.getValue().toString();
    }
}
