package org.dolphin.job.sample;

import java.util.concurrent.Delayed;
import java.util.concurrent.TimeUnit;

/**
 * Created by hanyanan on 2015/10/21.
 */
public class TestTrigerTime {
    /**
     * Returns the trigger time of a delayed action.
     */
    static long triggerTime(long delay) {
        return now() +
                ((delay < (Long.MAX_VALUE >> 1)) ? delay : overflowFree(delay));
    }

    /**
     * Constrains the values of all delays in the queue to be within
     * Long.MAX_VALUE of each other, to avoid overflow in compareTo.
     * This may occur if a task is eligible to be dequeued, but has
     * not yet been, while some other task is added with a delay of
     * Long.MAX_VALUE.
     */
    private static long overflowFree(long delay) {
        Delayed head = (Delayed) super.getQueue().peek();
        if (head != null) {
            long headDelay = head.getDelay(TimeUnit.NANOSECONDS);
            if (headDelay < 0 && (delay - headDelay < 0))
                delay = Long.MAX_VALUE + headDelay;
        }
        return delay;
    }
    public static void main(String[] argv) {

    }
}
