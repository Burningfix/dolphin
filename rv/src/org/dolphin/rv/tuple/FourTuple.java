package org.dolphin.rv.tuple;

/**
 * Created by hanyanan on 2015/3/17.
 */
public class FourTuple<T1, T2, T3, T4> extends ThreeTuple {
    public final T4 value4;

    public FourTuple(T1 value1, T2 value2, T3 value3, T4 value4) {
        super(value1, value2, value3);
        this.value4 = value4;
    }

    public static <T1, T2, T3, T4> FourTuple<T1, T2, T3, T4> tuple(T1 value1, T2 value2, T3 value3, T4 value4) {
        return new FourTuple<T1, T2, T3, T4>(value1, value2, value3, value4);
    }
}
