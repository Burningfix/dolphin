package org.dolphin.secret.calculator.character;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public interface OperationalCharacter {
    public Type getType();

    public BigDecimal getValue();

    enum Type {
        EOF, // 结束
        NUMBER, // 正常的数字
        LEFT_PARENTHESES,  // 左括号
        RIGHT_PARENTHESES,  // 右括号
        ADD, // 加号
        SUBTRACT, // 减号,
        MULTIPLICATION, // 乘法
        DIVISION,
    }
}
