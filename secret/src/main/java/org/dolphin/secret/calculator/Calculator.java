package org.dolphin.secret.calculator;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.util.Stack;

/**
 * Created by yananh on 2016/7/16.
 */
public class Calculator {

    public static void main(String[] argv) {
        System.out.println(compute(new CharArrayReader("(12+(12+2*4-6))".toCharArray())));
        System.out.println(compute(new CharArrayReader("4-2*3/(3-4*2)*3+3-5".toCharArray())));
        System.out.println(compute(new CharArrayReader("3.4*1.5/3-3+4".toCharArray())));
        System.out.println(compute(new CharArrayReader("3-4*5/9-4*5/6+3".toCharArray()))); // 0.444.......
        System.out.println(compute(new CharArrayReader("1+2*(2+4*3-8)".toCharArray())));
    }

    public static BigDecimal compute(CharSequence expression) throws IllegalExpressionException {
        return compute(new CharArrayReader(expression.toString().toCharArray()));
    }

    public static BigDecimal compute(CharArrayReader expression) throws IllegalExpressionException {
        final Stack<BigDecimal> numberStack = new Stack<BigDecimal>();
        while (expression.ready()) {
            char next = expression.shift();
            if (next == '+') {
                if (numberStack.isEmpty()) {
                    throw new IllegalExpressionException("+ cannot stat at first");
                }
                BigDecimal left = readNext(expression);
                numberStack.push(left);
            } else if (next == '-') {
                BigDecimal right = readNext(expression);
                numberStack.push(right.negate());
            } else if (next == '*') {
                if (numberStack.isEmpty()) {
                    throw new IllegalExpressionException("* before must have some number!");
                }
                BigDecimal right = readNext(expression);
                BigDecimal left = numberStack.pop();
                numberStack.push(right.multiply(left));
            } else if (next == '/' || next == '÷') {
                if (numberStack.isEmpty()) {
                    throw new IllegalExpressionException("/ before must have some number!");
                }
                BigDecimal right = readNext(expression);
                BigDecimal left = numberStack.pop();
                numberStack.push(left.divide(right, 10, RoundingMode.HALF_DOWN));
            } else if ((next >= '0' && next <= '9') || next == '.') {
                expression.unshift();
                BigDecimal left = readNext(expression);
                numberStack.push(left);
            } else if (next == '(') {
                if (!numberStack.isEmpty()) {
                    throw new IllegalExpressionException("( ");
                }
                expression.unshift();
                BigDecimal left = readNext(expression);
                numberStack.push(left);
            } else {
                throw new IllegalExpressionException("Compute unsppuort char " + next);
            }
        }

        BigDecimal res = new BigDecimal(0);
        for (BigDecimal bigDecimal : numberStack) {
            res = bigDecimal.add(res);
        }
        return res;
    }

    /**
     * 读取一个（）包含的计算式，开区间，不包含括号,第一个可用字符必须是左括号
     *
     * @return
     */
    public static CharArrayReader readerSubExpression(CharArrayReader parentInput) throws IllegalExpressionException {
        int leftParenthesesCount = 0;
        CharArrayReader.Builder builder = new CharArrayReader.Builder();
        while (parentInput.ready()) {
            char c = parentInput.shift();
            if (0 == leftParenthesesCount) {
                if (c != '(') {
                    throw new IllegalExpressionException("The first char of readerSubExpression must be '('!");
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
                        } else {
                            builder.append(c);
                        }
                        break;
                    default:
                        builder.append(c);
                        break;
                }
            }
        }
        throw new IllegalExpressionException("括号不匹配！");
    }

    public static BigDecimal readNext(CharArrayReader reader) throws IllegalExpressionException {
        if (!reader.ready()) {
            throw new IllegalExpressionException("readNext failed!");
        }
        char next = reader.shift();
        if ((next >= '0' && next <= '9') || next == '.') {
            reader.unshift();
            return readNumber(reader);
        }

        if (next == '(') {
            reader.unshift();
            return compute(readerSubExpression(reader));
        }
        throw new IllegalExpressionException("read next must be a number or expression during()");
    }

    /**
     * 读取一个数字, 支持数字/
     */
    public static BigDecimal readNumber(CharArrayReader reader) throws IllegalExpressionException {
        if (!reader.ready()) {
            throw new IllegalExpressionException("readNumber");
        }

        String s = "";
        while (reader.ready()) {
            char c = reader.shift();
            if (c == '.' || c >= '0' && c <= '9') {
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
            throw new IllegalExpressionException(s);
        }
    }
}
