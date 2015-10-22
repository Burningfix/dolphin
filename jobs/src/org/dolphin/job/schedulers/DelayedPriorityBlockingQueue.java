package org.dolphin.job.schedulers;

import java.util.*;
import java.util.concurrent.*;
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
        public void add(int index, T element) {
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
            if (index >= this.size()) return null;
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
            if (delay1 != delay2) {
                return delay1 - delay2 > 0 ? 1 : -1;
            }
            return o1.compareTo(o2);
        }
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
        if (null == t)
            throw new NullPointerException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.queue.add(t);
            T first = this.queue.get(0);
//            if (t == first) {
                available.signalAll();
//            }
        } finally {
            lock.unlock();
        }
        return false;
    }


    @Override
    public T take() throws InterruptedException {
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        try {
            while (true) {
                T first = this.queue.get(0);
                if (null == first) {
                    available.await();
                } else {
                    long delay = first.getDelay(ACCURATE_CLOCKS);
                    if (delay <= 0) {
                        this.queue.remove(first);
                        return first;
                    }

                    available.await(delay, ACCURATE_CLOCKS);
                }
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public T poll() {
        final Lock lock = this.lock;
        lock.lock();
        try{
            T first = this.queue.get(0);
            if(null == first || first.getDelay(TimeUnit.NANOSECONDS) > 0) {
                return null;
            }
            this.queue.remove(first);
            return first;
        }finally {
            lock.unlock();
        }
    }

    @Override
    public T poll(long timeout, TimeUnit unit) throws InterruptedException {
        final Lock lock = this.lock;
        lock.lockInterruptibly();
        long mostNanosDelay = unit.convert(timeout, TimeUnit.NANOSECONDS);
        try {
            while (true) {
                T first = this.queue.get(0);
                if (null == first) {
                    if (mostNanosDelay <= 0) {
                        return null;
                    }
                    mostNanosDelay = available.awaitNanos(mostNanosDelay); // 剩余时间
                } else {
                    long firstDelay = first.getDelay(TimeUnit.NANOSECONDS);
                    if(firstDelay <= 0) {
                        this.queue.remove(first);
                        return first;
                    }

                    if (mostNanosDelay <= 0) {
                        return null;
                    }

                    if(mostNanosDelay < firstDelay){
                        mostNanosDelay = available.awaitNanos(mostNanosDelay);
                    }else {
                        long timeLeft = available.awaitNanos(firstDelay);
                        mostNanosDelay -= firstDelay - timeLeft; // 剩余时间
                    }
                }
            }
        } finally {
            lock.unlock();
        }
    }


    @Override
    public T peek() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return this.queue.get(0);
        } finally {
            lock.unlock();
        }
    }

    @Override
    public void clear() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            this.queue.clear();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public boolean add(T t) {
        return offer(t);
    }

    @Override
    public Iterator<T> iterator() {
        final Lock lock = this.lock;
        lock.lock();
        try{
            return new DelayedIterator(queue.toArray((T[])null));
        }finally {
            lock.unlock();
        }
    }


    @Override
    public Object[] toArray() {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return queue.toArray();
        } finally {
            lock.unlock();
        }
    }

    @Override
    public <T1> T1[] toArray(T1[] a) {
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            return queue.toArray(a);
        } finally {
            lock.unlock();
        }
    }


    @Override
    public int remainingCapacity() {
        return Integer.MAX_VALUE;
    }

    @Override
    public int drainTo(Collection<? super T> c) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            T first;
            int n = 0;
            while ((first = this.queue.get(0)) != null) {
                c.add(first);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    @Override
    public int drainTo(Collection<? super T> c, int maxElements) {
        if (c == null)
            throw new NullPointerException();
        if (c == this)
            throw new IllegalArgumentException();
        final ReentrantLock lock = this.lock;
        lock.lock();
        try {
            T first;
            int n = 0;
            while (n < maxElements && (first = this.queue.get(0)) != null) {
                c.add(first);
                ++n;
            }
            return n;
        } finally {
            lock.unlock();
        }
    }

    private class DelayedIterator implements Iterator<T> {
        final T[] array;
        int cursor = 0;     // index of next element to return
        int lastRet = -1;   // index of last element, or -1 if no such

        DelayedIterator(T[] array) {
            this.array = array;
        }
        public boolean hasNext() {
            return cursor < array.length;
        }

        @Override
        public T next() {
            if (cursor >= array.length)
                throw new NoSuchElementException();
            lastRet = cursor;
            return array[cursor++];
        }

        @Override
        public void remove() {
            if (lastRet < 0)
                throw new IllegalStateException();
            DelayedPriorityBlockingQueue.this.remove(array[lastRet]);
            lastRet = -1;
        }
    }
}
