package org.dolphin.job.sample;

import org.dolphin.job.schedulers.DelayedPriorityBlockingQueue;
import org.dolphin.job.schedulers.PriorityScheduledThreadPoolExecutor;

import java.util.*;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

/**
 * Created by hanyanan on 2015/10/22.
 */
public class PriorityScheduledExecutorServerTest {
    private static final int CPU_COUNT = Runtime.getRuntime().availableProcessors();
    private static final int CORE_POOL_SIZE = CPU_COUNT + 1;
    private static final int MAXIMUM_POOL_SIZE = CPU_COUNT * 2 + 1;
    private static final int KEEP_ALIVE = 1;

    private static final ThreadFactory sThreadFactory = new ThreadFactory() {
        private final AtomicInteger count = new AtomicInteger(1);

        public Thread newThread(Runnable r) {
            return new Thread(r, "ComputationScheduler #" + count.getAndIncrement());
        }
    };

    private static final BlockingQueue<Runnable> sPoolWorkQueue =
            new PriorityBlockingQueue<Runnable>(128);

    /**
     * An {@link Executor} that can be used to execute tasks in parallel.
     */
    public static final Executor THREAD_POOL_EXECUTOR
            = new ThreadPoolExecutor(CORE_POOL_SIZE, MAXIMUM_POOL_SIZE, KEEP_ALIVE,
            TimeUnit.SECONDS, sPoolWorkQueue, sThreadFactory);

    static RejectedExecutionHandler rejectedExecutionHandler = new RejectedExecutionHandler(){
        @Override
        public void rejectedExecution(Runnable r, ThreadPoolExecutor executor) {
            System.out.println("rejectedExecution "+ r);
        }
    };

    static long now(){
        return System.currentTimeMillis();
    }

    static Random random = new Random();
    private static ComparableRunnable[] factory(int count){
        ComparableRunnable runnables[] = new ComparableRunnable[count];
        for(int i =0 ;i<count; ++i) {
            final int p = i;
            runnables[i] = new ComparableRunnable();
        }
        return runnables;
    }

    private static class ComparableRunnable implements Runnable, Comparable<ComparableRunnable> {
        static int S = 0;
        public int sequence = S++;

        @Override
        public int compareTo(ComparableRunnable o) {
            return sequence - o.sequence;
        }

        @Override
        public void run() {
            System.out.println(Thread.currentThread().getName() + " Runnable" + sequence + " Running " + now());
            try {
                Thread.sleep(Math.abs(random.nextInt())%2000);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            System.out.println(Thread.currentThread().getName() + " Runnable" + sequence + " Exiting " + now());
        }
    };

    private static void random(Object []content){
        if(null == content || content.length <= 0) return ;
        List list = new LinkedList(Arrays.asList(content));
        int index = 0;
        while(true){
            if(list.size() <= 0) return ;
            int position = (int) (System.nanoTime() % list.size());
            Object object = list.get(position);
            content[index++] = object;
            list.remove(object);
        }
    }

    public static void main(String argv[]) {
        System.out.println(Long.MAX_VALUE);
        ComparableRunnable[] runnables = factory(20);
        PriorityScheduledThreadPoolExecutor executor1 = new PriorityScheduledThreadPoolExecutor(
                4, 4, 60, TimeUnit.SECONDS,
                new DelayedPriorityBlockingQueue(), sThreadFactory, rejectedExecutionHandler);
        random(runnables);


        List<Future> futures = new ArrayList<Future>();

        long gdelay = 1000;
        for(ComparableRunnable runnable : runnables) {
//            int cmd = Math.abs(random.nextInt()) % 3;
            Future future;
            int cmd = 0;
            switch (cmd) {
                case 0: // 直接运行
                    System.out.println("Runnable" + runnable.sequence + " Normal " + now());
                    future = executor1.schedule(runnable, 0, TimeUnit.MILLISECONDS);
                    futures.add(future);
                    break;
                case 1: // 延时随机数运行
//                    long delay = Math.abs(random.nextInt()) % 2000 + 1000;
                    long delay = 2000;
                    System.out.println("Runnable" + runnable.sequence + " Delay to " + (now() + delay));
                    future = executor1.schedule(runnable, delay, TimeUnit.MILLISECONDS);
                    futures.add(future);
                    break;
                case 2: // 周期运行
                    long Periodic = Math.abs(random.nextInt()) % 1000 + 1000;
                    System.out.println("Runnable" + runnable.sequence + " isPeriodic to " + (now() + Periodic));
                    future = executor1.scheduleAtFixedRate(runnable, Periodic, Periodic, TimeUnit.NANOSECONDS);
                    futures.add(future);
                    break;
            }
        }

        try {
            Thread.sleep(1000 * 15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor1.pause();
        System.err.println("executor1 pause!");

        try {
            Thread.sleep(1000 * 10);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor1.resume();
        System.err.println("executor1 resume!");

        try {
            Thread.sleep(1000 * 15);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        for(Future future : futures) {
            future.cancel(true);
        }

        System.out.println("Cancel All Future!");

        try {
            Thread.sleep(1000 * 5);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        executor1.shutdown();
        System.err.println("executor1 shutdown!");
    }
}
