package org.dolphin.asyncjob;

/**
 * Created by yananh on 2016/1/1.
 */
public interface JobCallback {
    public <T> void resolve(T result);

    public void reject(Throwable error, Object ... unexpectedResult);
}
