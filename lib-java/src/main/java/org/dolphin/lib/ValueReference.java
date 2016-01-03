package org.dolphin.lib;

/**
 * Created by yananh on 2015/11/14.
 *
 * 用于应用的回掉
 */
public class ValueReference<T> {
    private T value;

    public ValueReference(){

    }

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
