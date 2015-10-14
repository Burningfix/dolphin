package org.dolphin.job;

import org.dolphin.job.operator.UntilOperator;

import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkNotNulls;

/**
 * Created by hanyanan on 2015/9/28.
 */
public class Job {
    public static final String TAG = "Job";

    protected Object object = null;
    protected final List<Operator> operatorList = new LinkedList<Operator>();
    protected JobErrorHandler errorHandler = null;
    protected Scheduler workScheduler = null;
    protected Scheduler observerScheduler = null;

    protected Job() {

    }

    public final Job insert(int position, Operator operator) {
        checkNotNull(operator);
        operatorList.add(position, operator);
        return this;
    }

    public final Job remove(int position) {
        operatorList.remove(position);
        return this;
    }

    public final Job replace(int position, Operator operator) {
        checkNotNull(operator);
        operatorList.remove(position);
        operatorList.add(position, operator);
        return this;
    }

    /**
     * 清除当前所有Operator
     */
    public final Job clear() {
        operatorList.clear();
        return this;
    }

    /**
     * until命令：循环执行，知道中断
     */
    public final Job until(Operator operator) {
        checkNotNull(operator);
        operatorList.add(new UntilOperator(operator));
        return this;
    }

    /**
     * until命令：循环执行，知道中断
     */
    public final Job until(Operator operator, boolean notifyNextCallback) {
        checkNotNull(operator);
        operatorList.add(new UntilOperator(operator, notifyNextCallback));
        return this;
    }

    public final Job append(Operator operator) {
        operatorList.add(operator);
        return this;
    }

    public final Job handleError(JobErrorHandler throwable) {
        errorHandler = throwable;
        return this;
    }

    public final Job workOn(Scheduler scheduler) {
        workScheduler = scheduler;
        return this;
    }

    public final Job observerOn(Scheduler scheduler) {
        observerScheduler = scheduler;
        return this;
    }

    public final Job subscribe(Observer observer) {

        return this;
    }

    public final Job setTag(Object object) {
        this.object = object;
        return this;
    }

    public final String description(){
        return "";
    }

    /**
     * 复制一个相同的job， 包括：
     * 1. 输入参数
     * 2. 所有的Operators
     * 3. 所有的Scheduler
     * 4. JobErrorHandler
     * 5.
     */
    public Job copy() {

        return null;
    }
}
