//package org.dolphin.secret.calculator;
//
//import org.dolphin.secret.calculator.character.AddOperationalCharacter;
//import org.dolphin.secret.calculator.character.NumberOperationalCharacter;
//import org.dolphin.secret.calculator.character.OperationalCharacter;
//import org.dolphin.secret.calculator.character.SubtractOperationalCharacter;
//import org.dolphin.secret.calculator.exception.BadNumberException;
//import org.dolphin.secret.calculator.exception.ReadFailedException;
//
//import java.math.BigDecimal;
//import java.util.Stack;
//
///**
// * Created by hanyanan on 2016/7/5.
// */
//public class Calculator {
//    private static Calculator ourInstance = new Calculator();
//
//    public static Calculator getInstance() {
//        return ourInstance;
//    }
//
//    private Calculator() {
//    }
//
//
//    public static void main(String[] argv) throws ReadFailedException, BadNumberException {
//        System.out.println(Calculator.getInstance().compute("1+2*3/4-5")); // -2.5
//        System.out.println(Calculator.getInstance().compute("3.4*1.5/3-3+4")); // 2.7
//        System.out.println(Calculator.getInstance().compute("3-4*5/9-4*5/6+3")); // 3
//        System.out.println(Calculator.getInstance().compute("1+2*(2+4*3-8)")); // 13
//    }
//
//    public BigDecimal compute(String expression) throws ReadFailedException, BadNumberException {
//        CharArrayReader reader = new CharArrayReader(expression.toCharArray());
//        return compute(reader).getValue();
//    }
//
//    /**
//     * 所有的操作都会收敛到加和减操作, 乘除会在收敛到一个运算符中
//     *
//     * @param expression
//     * @return
//     */
//    public OperationalCharacter compute(CharArrayReader expression) throws BadNumberException, ReadFailedException {
//        final Stack<OperationalCharacter> operationalCharacterStack = new Stack<OperationalCharacter>();
//        while (expression.ready()) {
//            char next = expression.shift();
//            if (next == '+') {
//                OperationalCharacter right = readAtomicOperationalCharacter(expression);
//                AddOperationalCharacter addOperationalCharacter = new AddOperationalCharacter(right);
//                operationalCharacterStack.push(addOperationalCharacter);
//            } else if (next == '-') {
//                OperationalCharacter right = readAtomicOperationalCharacter(expression);
//                SubtractOperationalCharacter subtractOperationalCharacter = new SubtractOperationalCharacter(right);
//                operationalCharacterStack.push(subtractOperationalCharacter);
//            } else if (next == '*') {
//                OperationalCharacter right = readAtomicOperationalCharacter(expression);
//                OperationalCharacter lastOperationalCharacter = operationalCharacterStack.pop();
//                NumberOperationalCharacter numberOperationalCharacter = new NumberOperationalCharacter(
//                        lastOperationalCharacter.getValue().multiply(right.getValue()));
//                operationalCharacterStack.push(numberOperationalCharacter);
//            } else if (next == '/') {
//                OperationalCharacter right = readAtomicOperationalCharacter(expression);
//                OperationalCharacter lastOperationalCharacter = operationalCharacterStack.pop();
//                NumberOperationalCharacter numberOperationalCharacter = new NumberOperationalCharacter(
//                        lastOperationalCharacter.getValue().divide(right.getValue(), 10, BigDecimal.ROUND_HALF_EVEN));
//                operationalCharacterStack.push(numberOperationalCharacter);
//            } else {
//                expression.unshift();
//                OperationalCharacter left = readAtomicOperationalCharacter(expression);
//                operationalCharacterStack.push(left);
//            }
//        }
//        BigDecimal res = new BigDecimal(0);
//        for (OperationalCharacter operationalCharacter : operationalCharacterStack) {
//            res = operationalCharacter.getValue().add(res);
//        }
//        return new NumberOperationalCharacter(res);
//    }
//
//    /**
//     * 读取一个数值或者将()内的计算成一个数值
//     *
//     * @param reader
//     * @return
//     * @throws BadNumberException
//     * @throws ReadFailedException
//     */
//    public NumberOperationalCharacter readAtomicOperationalCharacter(CharArrayReader reader)
//            throws BadNumberException, ReadFailedException {
//        char next = reader.shift();
//        reader.unshift();
//        if (next == '(') {
//            return new NumberOperationalCharacter(readShortExpression(reader));
//        }
//
//        if (next == '.' || next >= '0' && next <= '9') {
//            return new NumberOperationalCharacter(readNumber(reader));
//        }
//
//        throw new ReadFailedException("");
//    }
//
//    /**
//     * 一个短句子，只能是一个简单的表达式或者()内
//     *
//     * @param reader
//     * @return
//     */
//    public BigDecimal readShortExpression(CharArrayReader reader) throws ReadFailedException, BadNumberException {
//        char next = reader.shift();
//        if (next != '(') {
//            throw new ReadFailedException("");
//        }
//        StringBuilder stringBuilder = new StringBuilder();
//        while (reader.ready()) {
//            char c = reader.shift();
//            if (c == '(') {
//                reader.unshift();
//                stringBuilder.append(readShortExpression(reader).toString());
//                continue;
//            }
//            if (c == ')') {
//                BigDecimal res = compute(stringBuilder.toString());
//                return res;
//            }
//        }
//        // TODO: 非正常结束，没有找到右括号
//        throw new ReadFailedException("");
//    }
//
//    public BigDecimal readNumber(CharArrayReader reader) throws BadNumberException {
//        if (!reader.ready()) {
//            throw new BadNumberException("ReadNumber");
//        }
//
//        String s = "";
//        while (reader.ready()) {
//            char c = reader.shift();
//            if (c == '.' || c >= '0' && c <= '9') {
//                s += c;
//                continue;
//            }
//            reader.unshift();
//            break;
//        }
//        try {
//            BigDecimal bigDecimal = new BigDecimal(s);
//            return bigDecimal;
//        } catch (Throwable throwable) {
//            throw new BadNumberException(s);
//        }
//    }
//}
