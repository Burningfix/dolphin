package org.dolphin.job.tuple;

/**
 * Created by hanyanan on 2015/9/17.
 */
public class Tuples{
    public final Object[] values;

    public Tuples(Object... values) {
        this.values = values;
    }


    public static Object parseTuple(Object... values) {
        switch (values.length) {
            case 1:
                return new Tuple(values[0]);
            case 2:
                return new TwoTuple(values[0], values[1]);
            case 3:
                return new ThreeTuple(values[0], values[1], values[2]);
            case 4:
                return new FourTuple(values[0], values[1], values[2], values[3]);
            case 5:
                return new FiveTuple(values[0], values[1], values[2], values[3], values[4]);
            case 6:
                return new SixTuple(values[0], values[1], values[2], values[3], values[4], values[5]);
            default:
                return new Tuples(values);
        }
    }

}
