package org.dolphin.job;


/**
 * Created by hanyanan on 2015/11/10.
 */
public enum JobRunningResult {
    /**
     * Continue. When returned from a method then the entries in the directory should also
     * be visited.
     */
    CONTINUE,
    /**
     * Terminate.
     */
    TERMINATE,
}
