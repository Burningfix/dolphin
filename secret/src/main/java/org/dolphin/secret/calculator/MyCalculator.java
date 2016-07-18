package org.dolphin.secret.calculator;

import org.dolphin.secret.calculator.exception.IllegalExpressionException;
import org.dolphin.secret.calculator.exception.ReadFailedException;

import java.math.BigDecimal;
import java.util.Stack;

/**
 * Created by yananh on 2016/7/16.
 */
public class MyCalculator {


    public BigDecimal compute(CharArrayReader expression) throws ReadFailedException {
        final Stack<BigDecimal> numberStack = new Stack<BigDecimal>();
        return null;
    }

    /**
     * 读取一个（）包含的计算式，开区间，不包含括号,第一个可用字符必须是左括号
     *
     * @return
     */
    public CharArrayReader readerSubExpression(CharArrayReader parentInput) throws IllegalExpressionException {
        int leftParenthesesCount = 0;
        CharArrayReader.Builder builder = new CharArrayReader.Builder();
        while (parentInput.ready()) {
            char c = parentInput.shift();
            if (0 == leftParenthesesCount) {
                if (c != '(') {
                    throw new IllegalExpressionException("Read Sub Expression the first char must be '('!");
                } else {
                    leftParenthesesCount++;
                }
            } else {
                switch (c) {
                    case '(':
                        leftParenthesesCount++;
                        builder.append(c);
                        break;
                    case ')':
                        leftParenthesesCount--;
                        if (leftParenthesesCount == 0) {
                            return builder.build();
                        }
                        break;
                    default:
                        builder.append(c);
                        break;
                }
            }
        }
        throw new IllegalExpressionException("Unfinish expresion!");
    }

    /**
     * 读取一个数字, 支持数字/
     *
     * @throws ReadFailedException
     */
    public BigDecimal readNumber(CharArrayReader reader) throws ReadFailedException {
        if (!reader.ready()) {
            throw new ReadFailedException("readNumber");
        }

        String s = "";
        while (reader.ready()) {
            char c = reader.shift();
            if (c == '.' || c == '-' || c >= '0' && c <= '9') {
                s += c;
                continue;
            }
            reader.unshift();
            break;
        }
        try {
            BigDecimal bigDecimal = new BigDecimal(s);
            return bigDecimal;
        } catch (Throwable throwable) {
            throwable.printStackTrace();
            throw new ReadFailedException(s);
        }
    }
}
