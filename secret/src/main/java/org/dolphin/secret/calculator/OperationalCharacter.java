package org.dolphin.secret.calculator;

import java.math.BigDecimal;

/**
 * Created by yananh on 2016/7/6.
 */
public interface OperationalCharacter {
    enum Type {
        EOF, // 结束
        NUMBER, // 正常的数字
        LEFT_PARENTHESES,  // 左括号
        RIGHT_PARENTHESES,  // 右括号
        ADD, // 加号
        SUBTRACT, // 减号
        // 乘号和除号可以直接进行计算，不做特殊处理
    }

    public Type getType();

    public BigDecimal getValue();
}
