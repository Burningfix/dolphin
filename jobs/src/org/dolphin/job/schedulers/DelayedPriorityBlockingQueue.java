package org.dolphin.job.schedulers;

import java.util.AbstractQueue;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.Delayed;
import java.util.concurrent.PriorityBlockingQueue;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
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
    private final List<T> queue = new ArrayList<T>(INITIAL_CAPACITY) {
        @Override
        public boolean add(T delayed) {
            boolean res = super.add(delayed);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        @Override
        public void add(int index,  T element) {
            super.add(index, element);
            Collections.sort(queue, delayedComparator);
        }

        @Override
        public T remove(int index) {
            T res = super.remove(index);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        @Override
        public boolean remove(Object o) {
            boolean res = super.remove(o);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        @Override
        public void clear() {
            super.clear();
        }

        @Override
        public boolean addAll(Collection<? extends T> c) {
            boolean res = super.addAll(c);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        @Override
        public boolean addAll(int index, Collection<? extends T> c) {
            boolean res = super.addAll(index, c);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        @Override
        protected void removeRange(int fromIndex, int toIndex) {
            super.removeRange(fromIndex, toIndex);
            Collections.sort(queue, delayedComparator);
        }

        @Override
        public boolean removeAll(Collection<?> c) {
            boolean res = super.removeAll(c);
            Collections.sort(queue, delayedComparator);
            return res;
        }

        /**
         * 返回非空
         * @param index
         * @return
         */
        @Override
        public T get(int index) {
            if(index >= this.size()) return null;
            return super.get(index);
        }
    };

    /**
     * 排序使用的比较器
     */
    private static final DelayedComparator delayedComparator = new DelayedComparator();

    /**
     * Sort rules for delayed list.
     */
    private static class DelayedComparator implements Comparator<Delayed> {
        @Override
        public int compare(Delayed o1, Delayed o2) {
            long delay1 = o1.getDelay(ACCURATE_CLOCKS);
            long delay2 = o2.getDelay(ACCURATE_CLOCKS);
            if(delay1 != delay2) {
                return delay1 - delay2>0?1:-1;
            }
            return o1.compareTo(o2);
        }
    }

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
        offer(t);
    }

    @Override
    public boolean offer(T t, long timeout, TimeUnit unit) throws InterruptedException {
        return offer(t);  // 由于队列是无限长，所以可以直接插入，不需要等待超时时间
    }

    @Override
    public boolean offer(T t) {
        if(null == t)
            throw new NullPointerException();
        long delayTime = 0;
        final ReentrantLock lock = this.lock;
        lock.lock();
        try{
            this.queue.add(t);
            T first = this.queue.get(0);
            if(t == first) {
                available.signalAll();
            }
        }finally {
            lock.unlock();
        }
        return false;
    }


    @Override
    public T take() throws InterruptedException {
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        try{



        }finally {
            lock.unlock();
        }















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
    public T poll() {
        return null;
    }

    @Override
    public T peek() {
        return null;
    }
}
