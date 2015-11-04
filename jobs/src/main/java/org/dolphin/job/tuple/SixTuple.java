package org.dolphin.job.tuple;

/**
 * Created by hanyanan on 2015/3/17.
 */
public class SixTuple<T1, T2, T3, T4, T5, T6> extends FiveTuple<T1, T2, T3, T4, T5> {
    public final T6 value6;

    public SixTuple(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6) {
        super(value1, value2, value3, value4, value5);
        this.value6 = value6;
    }

    public static <T1, T2, T3, T4, T5, T6> SixTuple<T1, T2, T3, T4, T5, T6> tuple(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5, T6 value6) {
        return new SixTuple<T1, T2, T3, T4, T5, T6>(value1, value2, value3, value4, value5, value6);
    }
}
