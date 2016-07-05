package org.dolphin.secret.calculator;

import java.math.BigDecimal;

/**
 * Created by hanyanan on 2016/7/5.
 */
public class Calculator {
    private static Calculator ourInstance = new Calculator();

    public static Calculator getInstance() {
        return ourInstance;
    }

    private Calculator() {
    }

    public static BigDecimal calculate(String expression) {

    }

}
