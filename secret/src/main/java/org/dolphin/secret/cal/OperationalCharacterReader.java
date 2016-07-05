package org.dolphin.secret.cal;

import java.io.ByteArrayInputStream;

import javax.annotation.Nonnull;

/**
 * Created by yananh on 2016/7/6.
 */
public class OperationalCharacterReader {
    private final ByteArrayInputStream expression;

    public OperationalCharacterReader(String input) {
        this.expression = new ByteArrayInputStream(input.getBytes());
    }

    @Nonnull
    public OperationalCharacter read() throws ReadFailedException {
        while(expression.available() > 0) {


        }
        return null;
    }
}
