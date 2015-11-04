package org.dolphin.job.sample;

import java.util.concurrent.*;

/**
 * Created by hanyanan on 2015/10/19.
 */
public class ExecutorServiceTest {
    public static void main(String[] argv) {
        System.out.println(" ".equals(null));
        // 一个有7个作业线程的线程池，老大的老大找到一个管7个人的小团队的老大
        ScheduledExecutorService laoda = Executors.newScheduledThreadPool(7);
        //提交作业给老大，作业内容封装在Callable中，约定好了输出的类型是String。
        String outputs = null;
        try {
            System.out.println("Before running " + System.currentTimeMillis());
//            Future<String> future = laoda.submit(
//                    new Callable<String>() {
//                        public String call() throws Exception {
//                            System.out.println("Running " + System.currentTimeMillis());
//                            return "I am a task, which submited by the so called laoda, and run by those anonymous workers";
//                        }
//                        //提交后就等着结果吧，到底是手下7个作业中谁领到任务了，老大是不关心的。
//                    });
            Future future = laoda.scheduleAtFixedRate(
                    new Runnable() {
                        public void run() {
                            System.out.println("Running " + System.currentTimeMillis());
                            System.out.println("I am a task, which submited by the so called laoda, and run by those anonymous workers");
                        }
                        //提交后就等着结果吧，到底是手下7个作业中谁领到任务了，老大是不关心的。
                    }, 1, 1, TimeUnit.SECONDS);
            System.out.println("After submit " + System.currentTimeMillis());
//            future.cancel(true);
            System.out.println("isCancelled " + future.isCancelled());
            System.out.println("isDone " + future.isDone());
//            System.out.println("After submit get" + future.get());
//            System.out.println(outputs);
            Thread.sleep(3000);
            System.out.println("isCancelled " + future.isCancelled());
            System.out.println("isDone " + future.isDone());
            future.cancel(true);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}
