package org.dolphin.rx.tuple;

/**
 * Created by hanyanan on 2015/2/13.
 */
public class ThreeTuple<T1, T2, T3> extends TwoTuple<T1,T2> {
    public final T3 value3;
    public ThreeTuple(T1 value1, T2 value2, T3 value3) {
        super(value1, value2);
        this.value3 = value3;
    }

    public static <T1, T2, T3> ThreeTuple<T1, T2,T3>tuple(T1 value1, T2 value2, T3 value3){
        return new ThreeTuple<T1,T2,T3>(value1,value2,value3);
    }
}
