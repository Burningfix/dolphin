package org.dolphin.rx.tuple;

/**
 * Created by hanyanan on 2015/9/17.
 */
public class Tuples implements Tuple {
    public final Object[] values;

    public Tuples(Object ... values) {
        this.values = values;
    }

}
