package org.dolphin.lib;

/**
 * Created by yananh on 2015/11/14.
 */
public class ValueReference<T> {
    public T value;

    public ValueReference(T value){
        this.value = value;
    }

    public T getValue() {
        return value;
    }

    public void setValue(T value) {
        this.value = value;
    }
}