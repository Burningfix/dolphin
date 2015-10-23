package org.dolphin.job.operator;

import com.google.common.collect.Lists;
import org.dolphin.job.Operator;
import org.dolphin.job.tuple.Tuples;

import java.util.Iterator;

/**
 * Created by hanyanan on 2015/10/15.
 * <p/>
 * <p/>
 * 封装多个Operator到一起，<b>所有的operator都接受一个参数，输出一个或多个结果，并以Tuple的形式返回</b>
 */
public class OperatorWrapper implements Operator {
    private final Operator[] operators;

    public OperatorWrapper(Iterator<Operator> operators) {
        this.operators = Lists.newArrayList(operators).toArray(new Operator[]{});
    }

    /**
     * @param input 接受的输入参数
     * @return
     * @throws Throwable
     */
    public Object operate(Object input) throws Throwable {
        Object[] res = new Object[operators.length];

        for (int i = 0; i < operators.length; ++i) {
            res[i] = operators[i].operate(input);
        }

        return Tuples.parseTuple(res);
    }
}
