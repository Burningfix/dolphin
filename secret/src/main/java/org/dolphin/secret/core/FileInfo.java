package org.dolphin.secret.core;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class FileInfo {
    public String dom;
    public int softwareVersion;
    public int encodeVersion;
    public long originalFileLength;
    public long originalModifyTimeStamp;
    public String originalFileName;
    public String originalMimeType;
    Range originalFileHeaderRange;
    Range originalFileFooterRange;
    Range thumbnailRange;
    int transferSize;
    public long encodeTime;
    public byte[] extraTag;


    // 闭区间
    static class Range {
        public long offset;
        public int count;
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("Dom: ").append(dom).append("\n");
        sb.append("softwareVersion: ").append(softwareVersion).append("\n");
        sb.append("encodeVersion: ").append(encodeVersion).append("\n");
        sb.append("originalFileLength: ").append(originalFileLength).append("\n");
        sb.append("originalModifyTimeStamp: ").append(originalModifyTimeStamp).append("\n");
        sb.append("originalFileName: ").append(originalFileName).append("\n");
        sb.append("originalMimeType: ").append(originalMimeType).append("\n");
        sb.append("transferSize: ").append(transferSize).append("\n");
        sb.append("encodeTime: ").append(encodeTime).append("\n");
        return sb.toString();
    }
}
