package org.dolphin.job.schedulers;

import java.util.AbstractQueue;
import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.ReentrantLock;

/**
 * Created by hanyanan on 2015/10/20.
 */
public class DelayedPriorityBlockingQueue<T extends Delayed> extends AbstractQueue<T>
        implements BlockingQueue<T> {
    private static final String TAG = "DelayedPriorityBlockingQueue";
    private static final TimeUnit ACCURATE_CLOCKS = TimeUnit.NANOSECONDS;
    private static final int INITIAL_CAPACITY = 16;

    private final ReentrantLock lock = new ReentrantLock();
    /**
     * Condition signalled when a newer task becomes available at the
     * head of the queue or a new thread may need to become leader.
     */
    private final Condition available = lock.newCondition();

    /**
     * the order must keep follow rules:
     * 1. the front element must has low delay times.
     * 2. then order all elements as other rules.
     */
    private final BlockingQueue<Delayed> queue = new PriorityBlockingQueue<Delayed>(INITIAL_CAPACITY);


    @Override
    public Iterator<T> iterator() {
        return null;
    }

    @Override
    public int size() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return queue.size();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void put(T t) throws InterruptedException {

    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        long delayTime = unit.convert(timeout, ACCURATE_CLOCKS);









        return false;
    }

    @Override
    public T take() throws InterruptedException {
        return null;
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        return null;
    }

    @Override
    public int remainingCapacity() {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        return 0;
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        return 0;
    }

    @Override
    public boolean offer(T t) {
        return false;
    }

    @Override
    public T poll() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }
}
