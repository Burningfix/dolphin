package org.dolphin.job.tuple;

/**
 * Created by hanyanan on 2015/9/18.
 */
public class Tuple<T1> implements Tupleable {
    public final T1 value1;

    public Tuple(T1 value1) {
        this.value1 = value1;
    }
}
