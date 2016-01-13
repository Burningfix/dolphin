package org.dolphin.job.operator;

import com.google.gson.Gson;

import org.dolphin.job.Operator;

/**
 * Created by yananh on 2015/10/24.
 */
public class StringToGson<T> implements Operator<String, T> {
    public static final Gson gson = new Gson();
    private Class<T> clazz;
    public StringToGson(Class<T> clazz){
        this.clazz = clazz;
    }
    @Override
    public T operate(String input) throws Throwable {
        return gson.fromJson(input, this.clazz);
    }
}
