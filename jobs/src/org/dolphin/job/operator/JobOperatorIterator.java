package org.dolphin.job.operator;

import org.dolphin.job.Operator;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * Created by yananh on 2015/10/18.
 */
public class JobOperatorIterator implements Iterator<Operator> {
    private final List<Operator> operatorList = new LinkedList<Operator>();
    private int currCursor = 0;

    public JobOperatorIterator(List<Operator> operatorList) {
        this.operatorList.addAll(operatorList);
    }

    @Override
    public boolean hasNext() {
        synchronized (this) {
            if (currCursor >= operatorList.size()) {
                return false;
            }

            Operator operator = operatorList.get(currCursor);
            if (UntilOperator.class.isInstance(operator)) {
                UntilOperator untilOperator = (UntilOperator) operator;
                if (untilOperator.isLastReturnNull()) { // untilOperator执行结束
                    currCursor++;
                    return hasNext();
                }
            }

            return true;
        }
    }

    @Override
    public Operator next() {
        synchronized (this) {
            Operator operator = operatorList.get(currCursor);
            if (UntilOperator.class.isInstance(operator)) {
                UntilOperator untilOperator = (UntilOperator) operator;
                if (untilOperator.isLastReturnNull()) { // untilOperator执行结束
                    currCursor++;
                    return next();
                }
            }

            return operator;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not support remove operation for OperatorIterator!");
    }
}
