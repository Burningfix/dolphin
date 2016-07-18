package org.dolphin.secret.calculator.character;

import org.dolphin.secret.calculator.character.OperationalCharacter;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public class SubtractOperationalCharacter implements OperationalCharacter {
    private final OperationalCharacter operationalCharacter;

    public SubtractOperationalCharacter(OperationalCharacter operationalCharacter) {
        this.operationalCharacter = operationalCharacter;
    }

    @Override
    public Type getType() {
        return Type.SUBTRACT;
    }

    @Override
    public BigDecimal getValue() {
        return BigDecimal.valueOf(0).subtract(operationalCharacter.getValue());
    }

    @Override
    public String toString() {
        return "SUB " + getValue().toString();
    }
}
