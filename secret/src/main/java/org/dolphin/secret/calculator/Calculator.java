package org.dolphin.secret.calculator;

import org.dolphin.secret.calculator.exception.BadNumberException;
import org.dolphin.secret.calculator.exception.ReadFailedException;

import java.io.IOException;
import java.math.BigDecimal;
import java.util.Stack;

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


    public static void main(String[] argv) throws ReadFailedException, IOException, BadNumberException {
        System.out.println(Calculator.getInstance().compute("1+2*3/4-5")); // -2.5
        System.out.println(Calculator.getInstance().compute("3.4*1.5/3-3+4")); // 2.7
        System.out.println(Calculator.getInstance().compute("3-4+5*6/4-6/3+4-5/6+3")); // 3
    }

    public BigDecimal compute(String expression) throws ReadFailedException, BadNumberException, IOException {
        CharArrayReader reader = new CharArrayReader(expression.toCharArray());
        return compute(reader).getValue();
    }

    /**
     * 所有的操作都会收敛到加和减操作, 乘除会在收敛到一个运算符中
     *
     * @param expression
     * @return
     */
    public OperationalCharacter compute(CharArrayReader expression) throws BadNumberException, ReadFailedException {
        final Stack<OperationalCharacter> operationalCharacterStack = new Stack<OperationalCharacter>();
        while (expression.ready()) {
            char next = expression.shift();
            if (next == '+') {
                OperationalCharacter right = readNumberOperationalCharacter(expression);
                AddOperationalCharacter addOperationalCharacter = new AddOperationalCharacter(right);
                operationalCharacterStack.push(addOperationalCharacter);
            } else if (next == '-') {
                OperationalCharacter right = readNumberOperationalCharacter(expression);
                SubtractOperationalCharacter subtractOperationalCharacter = new SubtractOperationalCharacter(right);
                operationalCharacterStack.push(subtractOperationalCharacter);
            } else if (next == '*') {
                OperationalCharacter right = readNumberOperationalCharacter(expression);
                OperationalCharacter lastOperationalCharacter = operationalCharacterStack.pop();
                NumberOperationalCharacter numberOperationalCharacter = new NumberOperationalCharacter(
                        lastOperationalCharacter.getValue().multiply(right.getValue()));
                operationalCharacterStack.push(numberOperationalCharacter);
            } else if (next == '/') {
                OperationalCharacter right = readNumberOperationalCharacter(expression);
                OperationalCharacter lastOperationalCharacter = operationalCharacterStack.pop();
                NumberOperationalCharacter numberOperationalCharacter = new NumberOperationalCharacter(
                        lastOperationalCharacter.getValue().divide(right.getValue()));
                operationalCharacterStack.push(numberOperationalCharacter);
            } else if (next == '(') {
                expression.unshift();
                OperationalCharacter left = compute(expression);
                operationalCharacterStack.push(left);
            } else if (next == ')') {
                break;
            } else if (next >= '0' && next <= '9') {
                expression.unshift();
                NumberOperationalCharacter number = readNumberOperationalCharacter(expression);
                operationalCharacterStack.push(number);
            } else {
                throw new ReadFailedException("Not support char " + next);
            }
        }
        BigDecimal res = new BigDecimal(0);
        for (OperationalCharacter operationalCharacter : operationalCharacterStack) {
            res = operationalCharacter.getValue().add(res);
        }
        return new NumberOperationalCharacter(res);
    }

    /**
     * 读取一个数值或者将()内的计算成一个数值
     *
     * @param reader
     * @return
     * @throws BadNumberException
     * @throws ReadFailedException
     */
    public NumberOperationalCharacter readNumberOperationalCharacter(CharArrayReader reader)
            throws BadNumberException, ReadFailedException {
        String s = "";
        while (reader.ready()) {
            char next = reader.shift();
            if (next == '(') {
                reader.unshift();
                return new NumberOperationalCharacter(compute(reader).getValue());
            }
            if (next != '.' && next < '0' || next > '9') {
                reader.unshift();
                break;
            }
            s += next;
        }
        try {
            BigDecimal bigDecimal = new BigDecimal(s);
            return new NumberOperationalCharacter(bigDecimal);
        } catch (Throwable throwable) {
            throw new BadNumberException();
        }
    }
}
