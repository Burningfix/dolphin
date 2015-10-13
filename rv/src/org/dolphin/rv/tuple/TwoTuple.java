package org.dolphin.rv.tuple;

import java.util.List;

/**
 * Created by hanyanan on 2015/2/13.
 */
public class TwoTuple<T1, T2> implements Tuple {
    public final T1 value1;
    public final T2 value2;

    public TwoTuple(T1 value1, T2 value2) {
        this.value1 = value1;
        this.value2 = value2;
        List set;
    }

    public static <T1, T2> TwoTuple<T1, T2> tuple(T1 value1, T2 value2) {
        return new TwoTuple<T1, T2>(value1, value2);
    }
}
