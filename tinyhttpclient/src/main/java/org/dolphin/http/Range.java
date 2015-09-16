package org.dolphin.http;

/**
 * Created by hanyanan on 2015/5/14.
 */
public class Range {
    private final long start;
    private final long end;
    private final long fullLength;

    Range(long start, long end, long fullLength) {
        this.start = start;
        this.end = end;
        this.fullLength = fullLength;
    }

    public long getFullLength(){
        return fullLength;
    }

    public long getStart(){
        return start;
    }

    public long getEnd(){
        if(end <= 0 ){
            if(fullLength > 0) {
                return fullLength - 1;
            }
            return end;
        } else {
            return end;
        }

    }
}
