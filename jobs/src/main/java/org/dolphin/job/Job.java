package org.dolphin.job;

import org.dolphin.job.operator.OperatorWrapper;
import org.dolphin.job.operator.UntilOperator;
import org.dolphin.job.schedulers.Scheduler;
import org.dolphin.job.schedulers.Schedulers;
import org.dolphin.lib.util.ValueUtil;

import java.io.Serializable;
import java.util.LinkedList;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicLong;

import static org.dolphin.lib.Preconditions.checkArgument;
import static org.dolphin.lib.Preconditions.checkNotNull;

/**
 * Created by hanyanan on 2015/9/28.
 *
 * 当需要释放内存时，可以把Job可以序列化，但是如下参数是不会被序列化的：输入，输出，成功/失败/取消的回调,
 * 这样当需要恢复的时候，需要重新设置输入参数和回调接口。
 *
 */
public class Job<I, O> implements Comparable<Job> , Serializable{
    public static final String TAG = "Job";
    /**
     * 序列号生成器
     */
    private static final AtomicLong SEQUENCE_CREATOR = new AtomicLong(0);
    /**
     * 当前Job的序列号
     */
    public long sequence = SEQUENCE_CREATOR.getAndIncrement();

    /**
     * 当前Job是否取消了
     */
    public final AtomicBoolean aborted = new AtomicBoolean(false);
    /**
     * 是否还能够修改属性，比如添加监听，修改描述属性......
     */
    public final AtomicBoolean frozen = new AtomicBoolean(false);
    /**
     * Job的tag，用于保存临时信息
     */
    protected Object tag = null;
    protected final List<Operator> operators = new LinkedList<Operator>();
    protected Scheduler workScheduler = Schedulers.computation();
    protected Scheduler observerScheduler = null;
    protected transient I input;
    protected transient O output;
    protected JobWorkPolicy workPolicy = null;
    protected transient Callback1 resultCallback;
    protected transient Callback2 errorCallback;
    protected transient Callback0 cancelCallback;
    protected String logNode;

    public Job(I input) {
        this.input = input;
    }

    public Job(){

    }

    public Job setInput(I input){
        this.input = input;
        return this;
    }

    /**
     * 返回当前job的输入
     */
    public I getInput() {
        return this.input;
    }

//    public O get() {
//        if (null == output) {
//            output = JobEngine.instance().getResult(this);
//        }
//        return output;
//    }

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
    public final <T extends UntilOperator> Job until(UntilOperator operator) {
        checkNotNull(operator);
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        operators.add(operator);
        return this;
    }

    /**
     * until命令：循环执行，知道中断
     */
    public final <T extends UntilOperator> Job until(UntilOperator operator, boolean notifyNextCallback) {
        checkNotNull(operator);
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        operators.add(operator);
        return this;
    }

    public final Job then(Operator operator) {
        checkNotNull(operator);
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        operators.add(operator);
        return this;
    }

    /**
     * 一个输入，多个输出，每一个Operator都是同一个输入参数和不同的输出，将多个输出打包成一个Tuple传递到下一次输出。
     *
     * @return
     */
    public final Job merge(Iterable<Operator> operators) {
        checkNotNull(operators);
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        this.operators.add(new OperatorWrapper(operators));
        return this;
    }

    public final List<Operator> getOperators() {
        return new LinkedList<Operator>(operators);
    }

    /**
     * 每个Job最多只能有一个finalize Operator，无论是成功还是失败，都会调用此Operator。
     *
     * @param operator，接受任何的输入，不产生任何输出。当有异常发生时（或用户abort当前Job）， 输入是异常发生前的产生的中间变量；当执行成功时则
     * @return
     */
    public final Job finalize(Operator<?, Void> operator) {
        // finalize release resources
        return this;
    }

    public final Job workOn(Scheduler scheduler) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        workScheduler = scheduler;
        return this;
    }

    public final Scheduler getWorkScheduler() {
        return workScheduler;
    }

    public final Job callbackOn(Scheduler scheduler) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change properties.");
        // 可以使用一个运算符进行代替
        observerScheduler = scheduler;
        return this;
    }

    public final Scheduler getCallbackScheduler() {
        return observerScheduler;
    }

    public Job workDelayed(long millTimes) {
        JobEngine.loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(millTimes, TimeUnit.MILLISECONDS)));
        frozen.set(true);
        return this;
    }

    public Job workDelayed(long delay, TimeUnit timeUnit) {
        JobEngine.loadJob(setWorkPolicy(JobWorkPolicy.delayWorkPolicy(delay, timeUnit)));
        frozen.set(true);
        return this;
    }

    public Job work() {
        JobEngine.loadJob(setWorkPolicy(JobWorkPolicy.immediately()));
        frozen.set(true);
        return this;
    }

    public Job workPeriodic(long initDelay, long periodic, TimeUnit timeUnit) {
        JobEngine.loadJob(setWorkPolicy(JobWorkPolicy.workPolicy(initDelay, periodic, timeUnit)));
        frozen.set(true);
        return this;
    }

    public JobWorkPolicy getWorkPolicy() {
        return workPolicy;
    }

    public Job setWorkPolicy(JobWorkPolicy workPolicy) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change any property.");
        this.workPolicy = workPolicy;
        return this;
    }

    public final Job result(Callback1 action) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change any property.");
        this.resultCallback = action;
        return this;
    }

    public final Job error(Callback2 action) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change any property.");
        this.errorCallback = action;
        return this;
    }

    public final Job cancel(Callback0 callback) {
        this.cancelCallback = callback;
        return this;
    }

    public final Callback1 getResultCallback() {
        return resultCallback;
    }

    public final Callback2 getErrorCallback() {
        return errorCallback;
    }

    public final Job setCancelCallback(Callback0 cancelCallback) {
        this.cancelCallback = cancelCallback;
        return this;
    }

    public final Callback0 getCancelCallback() {
        return cancelCallback;
    }

    public final Job abort() {
        frozen.set(false);
        aborted.set(true);
        JobEngine.abort(this);
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

    /**
     * 设置当前node的log的信息，一般如下Request.SubRequest.Session1
     */
    public Job setLogNode(String node) {
        checkNotNull(node);
        checkArgument(!frozen.get(), "The job has frozen, Cannot change any property.");
        this.logNode = node;
        return this;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Job");
        if (!ValueUtil.isEmpty(logNode)) {
            sb.append('[').append(logNode).append(']');
        }
        sb.append(":").append(sequence).append("\t");
        if (null != input) {
            sb.append("{").append(input.toString()).append("}");
        }
        return sb.toString();
    }

    public final Job setTag(Object object) {
        checkArgument(!frozen.get(), "The job has frozen, Cannot change any property.");
        this.tag = object;
        return this;
    }

    public final Object getTag() {
        return this.tag;
    }


    public interface Callback0 {
        public void call();
    }

    public interface Callback1<T> {
        public void call(T result);
    }

    public interface Callback2<T> {
        public void call(Throwable throwable, T... unexpectedResult);
    }


    private static class Callback0Wrapper implements Callback0 {

        @Override
        public void call() {

        }
    }
}
