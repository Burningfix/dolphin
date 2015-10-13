package org.dolphin.rv.tuple;

/**
 * Created by hanyanan on 2015/3/17.
 */
public class FiveTuple<T1, T2, T3, T4, T5> extends FourTuple<T1, T2, T3, T4> {
    public final T5 value5;

    public FiveTuple(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) {
        super(value1, value2, value3, value4);
        this.value5 = value5;
    }

    public static <T1, T2, T3, T4, T5> FiveTuple<T1, T2, T3, T4, T5> tuple(T1 value1, T2 value2, T3 value3, T4 value4, T5 value5) {
        return new FiveTuple<T1, T2, T3, T4, T5>(value1, value2, value3, value4, value5);
    }
}
