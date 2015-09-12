package org.dolphin.http;

/**
 * Created by hanyanan on 2015/5/14.
 */
public class TimeStatus {
    public long getEstablishConnectionTime() {
        return establishConnectionTime;
    }

    public void setEstablishConnectionTime(long establishConnectionTime) {
        this.establishConnectionTime = establishConnectionTime;
    }

    public long getOutBoundClosedTime() {
        return outBoundClosedTime;
    }

    public void setOutBoundClosedTime(long outBoundClosedTime) {
        this.outBoundClosedTime = outBoundClosedTime;
    }

    public long getReadyToReadTime() {
        return readyToReadTime;
    }

    public void setReadyToReadTime(long readyToReadTime) {
        this.readyToReadTime = readyToReadTime;
    }

    public long getCloseConnectionTime() {
        return closeConnectionTime;
    }

    public void setCloseConnectionTime(long closeConnectionTime) {
        this.closeConnectionTime = closeConnectionTime;
    }

    //stream establish connect time
    private long establishConnectionTime;
    //the outbound close time
    private long outBoundClosedTime;
    //The time of ready data from input stream
    private long readyToReadTime;
    //The time of close connection time.
    private long closeConnectionTime;
}
