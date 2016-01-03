package org.dolphin.job;

import org.dolphin.job.operator.JobOperatorIterator;
import org.dolphin.job.operator.OperatorWrapper;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.schedulers.JobEngine;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.ValueUtil;

import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;

import static org.dolphin.lib.Preconditions.checkArgument;
import static org.dolphin.lib.Preconditions.checkNotNull;
import static org.dolphin.lib.Preconditions.checkState;

/**
 * Created by hanyanan on 2015/9/28.
 */
public class Job implements Comparable<Job> {
    public static final String TAG = "Job";
    private static long sSequence = 0;

    public long sequence = sSequence++;

    public final AtomicBoolean aborted = new AtomicBoolean(false);
    /**
     *  是否还能够修改属性，比如添加监听，修改描述属性......
     */
    public final AtomicBoolean freezing  = new AtomicBoolean(false);
    protected Object tag = null;
    protected final List<Operator> operatorList = new LinkedList<Operator>();
    protected Scheduler workScheduler = null;
    protected Scheduler observerScheduler = null;
    protected Object input = null;
    protected JobWorkPolicy workPolicy = null;
    protected Action1 resultAction;
    protected Action2 errorAction;
    protected String traceNode;

    public Job(Object input) {
        this.input = input;
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
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        operatorList.add(new UntilOperator(operator));
        return this;
    }

    /**
     * until命令：循环执行，知道中断
     */
    public final Job until(Operator operator, boolean notifyNextCallback) {
        checkNotNull(operator);
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        operatorList.add(new UntilOperator(operator, notifyNextCallback));
        return this;
    }

    public final Job then(Operator operator) {
        checkNotNull(operator);
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        operatorList.add(operator);
        return this;
    }

    /**
     * 一个输入，多个输出，每一个Operator都是同一个输入参数和不同的输出，将多个输出打包成一个Tuple传递到下一次输出。
     *
     * @return
     */
    public final Job merge(Iterable<Operator> operators) {
        checkNotNull(operators);
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        operatorList.add(new OperatorWrapper(operators));
        return this;
    }

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

    public final Job workOn(Scheduler scheduler) {
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        workScheduler = scheduler;
        return this;
    }

    public final Scheduler getWorkScheduler() {
        return workScheduler;
    }

    public final Job observerOn(Scheduler scheduler) {
        checkArgument(!freezing.get(), "The job has frozen, Cannot change properties.");
        // 可以使用一个运算符进行代替
        observerScheduler = scheduler;
        return this;
    }

    public final Scheduler getObserverScheduler() {
        return observerScheduler;
    }

    public Job workDelayed(long millTimes) {
        freezing.set(true);
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(millTimes, TimeUnit.MILLISECONDS)));
        return this;
    }

    public Job workDelayed(long delay, TimeUnit timeUnit) {
        freezing.set(true);
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(delay, timeUnit)));
        return this;
    }

    public Job work() {
        freezing.set(true);
        JobEngine.instance().loadJob(this);
        return this;
    }

    public Job workPeriodic(long initDelay, long periodic, TimeUnit timeUnit) {
        freezing.set(true);
        JobEngine.instance().loadJob(setWorkPolicy(JobWorkPolicy.workPolicy(initDelay, periodic, timeUnit)));
        return this;
    }

    public final Job setTag(Object object) {
        checkArgument(!freezing.get(), "The job has frozen, Cannot change any property.");
        this.tag = object;
        return this;
    }

    public JobWorkPolicy getWorkPolicy() {
        return workPolicy;
    }

    public Job setWorkPolicy(JobWorkPolicy workPolicy) {
        checkArgument(!freezing.get(), "The job has frozen, Cannot change any property.");
        this.workPolicy = workPolicy;
        return this;
    }

    public final Job result(Action1 action){
        checkArgument(!freezing.get(), "The job has frozen, Cannot change any property.");
        this.resultAction = action;
        return this;
    }

    public final Job error(Action2 action){
        checkArgument(!freezing.get(), "The job has frozen, Cannot change any property.");
        this.errorAction = action;
        return this;
    }

    public final Job abort() {
        freezing.set(false);
        aborted.set(true);
        JobEngine.instance().abort(this);
        return this;
    }

    public final boolean isAborted() {
        return aborted.get();
    }


    @Override
    public int compareTo(Job o) {
        long diff = sequence - o.sequence;
        return diff < 0 ? -1 : (diff == 0 ? 0 : 1);
    }

    public Job setTraceNode(String node) {
        checkNotNull(node);
        checkArgument(!freezing.get(), "The job has frozen, Cannot change any property.");
        this.traceNode = node;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Job");
        if(!ValueUtil.isEmpty(traceNode)){
            sb.append('[').append(traceNode).append(']');
        }
        if (null != input) {
            sb.append("{" + input.toString() + "} With Sequence \t" + sequence);
        }
        return sb.toString();
    }

    public final Object getTag(){
        return this.tag;
    }

    public abstract class Action1 <T>{
        public void call(T result){
            // Default implement
        }
    }

    public abstract class Action2 <T>{
        public void call(Throwable throwable, T ... unexpectedResult){
            // Default implement
        }
    }
}
