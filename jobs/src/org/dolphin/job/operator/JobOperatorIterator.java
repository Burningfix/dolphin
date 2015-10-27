package org.dolphin.job.operator;

import org.dolphin.job.util.Log;
import org.dolphin.job.Operator;
import java.util.Iterator;
import java.util.List;

/**
 * Created by yananh on 2015/10/18.
 */
public class JobOperatorIterator implements Iterator<Operator> {
    private final List<Operator> operatorList;
    private int currCursor = 0;

    public JobOperatorIterator(List<Operator> operatorList) {
        this.operatorList = operatorList;
    }

    @Override
    public boolean hasNext() {
        synchronized (this) {
            Log.d("DDD", "hasNext1");
            if (currCursor >= operatorList.size()) {
                return false;
            }
            Log.d("DDD", "hasNext2");

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
            Log.d("DDD", "next");
            Operator operator = operatorList.get(currCursor);
            if (UntilOperator.class.isInstance(operator)) {
                UntilOperator untilOperator = (UntilOperator) operator;
                if (untilOperator.isLastReturnNull()) { // untilOperator执行结束
                    currCursor++;
                    return next();
                }
            }else{
                currCursor++;
            }

            return operator;
        }
    }

    @Override
    public void remove() {
        throw new UnsupportedOperationException("Not support remove operation for OperatorIterator!");
    }
}
