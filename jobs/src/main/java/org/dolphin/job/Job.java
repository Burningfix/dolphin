package org.dolphin.job;

import org.dolphin.job.operator.JobOperatorIterator;
import org.dolphin.job.operator.OperatorWrapper;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.schedulers.JobEngine;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dolphin.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/9/28.
 */
public class Job implements Comparable<Job> {
    public static final String TAG = "Job";
    private static long sSequence = 0;

    public long sequence = sSequence++;

    public AtomicBoolean aborted = new AtomicBoolean(false);

    protected Object tag = null;
    protected final List<Operator> operatorList = new LinkedList<Operator>();
    protected JobErrorHandler errorHandler = null;
    protected Scheduler workScheduler = null;
    protected Scheduler observerScheduler = null;
    protected Object input = null;
    protected Object output = null;
    protected Observer observer;
    protected JobWorkPolicy workPolicy;
    @Deprecated protected JobRunningState runningState;

    public Job(Object input) {
        this.input = input;
    }

    public final Object getInput() {
        return input;
    }

    public final Object getOutput() {
        return output;
    }

    /**
     * 清除当前所有Operator
     */
    public final Job clear() {
        operatorList.clear();
        return this;
    }


    /**
     * until命令：循环执行，直到结束。
     * until命令会运行接受输入，产生的输出会作为中间结果。<b>当此运算结束后，作为此结果的输入作为输出。</b>
     * 例如http下载时，需要不断的回调当前进度，如下:{@code
     * job.until(new HttpCopyOperator()) // operator.operate(TwoTuple(HttRequest, fileOutputStream))会调用多次
     * // 但是世界上输入参数是固定的，每次的输出作为进度；
     * // 结束后，TwoTuple(HttRequest, fileOutputStream)仍然会作为下一个Operator的输入.
     * .append(new CloseHttpOperator()) // operator.operate(TwoTuple(HttRequest, fileOutputStream))关闭输出输出流
     * }
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

    /**
     * 一个输入，多个输出，每一个Operator都是同一个输入参数和不同的输出，将多个输出打包成一个Tuple传递到下一次输出。
     *
     * @param operatorIterator
     * @return
     */
    public final Job merge(Iterable<Operator> operators) {
        if (null == operators) {
            throw new NullPointerException("");
        }
        operatorList.add(new OperatorWrapper(operators));
        return this;
    }

    //    public final Iterator<Operator> getOperatorList() {
//        return new JobOperatorIterator(operatorList);
//    }


    public final List<Operator> getOperatorList() {
        return new LinkedList<Operator>(operatorList);
    }

    /**
     * 每个Job最多只能有一个finalize Operator，无论是成功还是失败，都会调用此Operator。
     *
     * @param operator，接受任何的输入，不产生任何输出。当有异常发生时（或用户abort当前Job）， 输入是异常发生前的产生的中间变量；当执行成功时则
     * @return
     */
    public final Job finalize(Operator<?, Void> operator) {

        return this;
    }

    public final Job handleError(JobErrorHandler throwable) {
        errorHandler = throwable;
        return this;
    }

    public final JobErrorHandler getErrorHandler() {
        return errorHandler;
    }

    public final Job workOn(Scheduler scheduler) {
        workScheduler = scheduler;
        return this;
    }

    public final Scheduler getWorkScheduler() {
        return workScheduler;
    }

    public final Job observerOn(Scheduler scheduler) {
        // 可以使用一个运算符进行代替
        observerScheduler = scheduler;

        return this;
    }

    public final Scheduler getObserverScheduler() {
        return observerScheduler;
    }

    public final Job observer(Observer observer) {
        this.observer = observer;

        return this;
    }

    public final Observer getObserver() {
        return this.observer;
    }

    public Job workDelayed(long millTimes) {
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(millTimes, TimeUnit.MILLISECONDS)));
        return this;
    }

    public Job workDelayed(long delay, TimeUnit timeUnit) {
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(delay, timeUnit)));
        return this;
    }

    public Job work() {
        JobEngine.instance().loadJob(this);
        return this;
    }

    public Job workPeriodic(long initDelay, long periodic, TimeUnit timeUnit) {
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.workPolicy(initDelay, periodic, timeUnit)));
        return this;
    }

    public final Job setTag(Object object) {
        this.tag = object;
        return this;
    }

    public JobWorkPolicy getWorkPolicy() {
        return workPolicy;
    }

    public Job setWorkPolicy(JobWorkPolicy workPolicy) {
        this.workPolicy = workPolicy;
        return this;
    }

    @Deprecated
    public JobRunningState getRunningState() {
        return runningState;
    }

    @Deprecated
    public Job setRunningState(JobRunningState runningState) {
        this.runningState = runningState;
        return this;
    }

    public Job description(String description) {
        // TODO: storage current description
        return this;
    }

    public String description() {
        return "";
    }

    public final Job abort() {
        aborted.set(true);
        JobEngine.instance().abort(this);
        return this;
    }


    public final boolean isAborted() {
        return aborted.get();
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
        // TODO
        return null;
    }

    @Override
    public int compareTo(Job o) {
        long diff = sequence - o.sequence;
        return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
    }

    @Override
    public String toString() {
        if (null != input) {
            return "Job {" + input.toString() + "} With Sequence \t" + sequence;
        }
        return super.toString();
    }
}
