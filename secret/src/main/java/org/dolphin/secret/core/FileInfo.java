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
    public String proguardFileName;
    public String originalMimeType;
    Range originalFileHeaderRange;
    Range originalFileFooterRange;
    Range thumbnailRange;
    int transferSize;
    public long encodeTime;
    public byte[] extraTag;

    @Override
    public int hashCode() {
        return originalFileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!FileInfo.class.isInstance(o)) {
            return false;
        }
        FileInfo other = (FileInfo) o;
        return this.originalFileName.equals(other.originalFileName);
    }

    public boolean isVideoType() {
        if (null == originalMimeType) return false;
        if (originalMimeType.startsWith("video")) {
            return true;
        }
        return false;
    }

    public boolean isPhotoType() {
        if (null == originalMimeType) return false;
        if (originalMimeType.startsWith("image")) {
            return true;
        }
        return false;
    }

    public boolean isAudioType() {
        if (null == originalMimeType) return false;
        if (originalMimeType.startsWith("audio")) {
            return true;
        }
        return false;
    }

    // 闭区间
    static class Range {
        public long offset;
        public int count;

        @Override
        public String toString() {
            return "[offset: " + offset + ", count: " + count + "]";
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder();
        sb.append("{\n\tDom: ").append(dom).append("\n");
        sb.append("\tsoftwareVersion: ").append(softwareVersion).append("\n");
        sb.append("\tencodeVersion: ").append(encodeVersion).append("\n");
        sb.append("\toriginalFileLength: ").append(originalFileLength).append("\n");
        sb.append("\toriginalModifyTimeStamp: ").append(originalModifyTimeStamp).append("\n");
        sb.append("\toriginalFileName: ").append(originalFileName).append("\n");
        sb.append("\tproguardFileName: ").append(proguardFileName).append("\n");
        sb.append("\toriginalMimeType: ").append(originalMimeType).append("\n");
        sb.append("\ttransferSize: ").append(transferSize).append("\n");
        sb.append("\tencodeTime: ").append(encodeTime).append("\n");
        sb.append("\toriginalFileHeaderRange: ").append(originalFileHeaderRange).append("\n");
        sb.append("\toriginalFileFooterRange: ").append(originalFileFooterRange).append("\n");
        sb.append("\tthumbnailRange: ").append(thumbnailRange).append("\n");
        if (null != extraTag) {
            sb.append("\textra Size: ").append(extraTag.length).append("\n");
        }
        sb.append("}");
        return sb.toString();
    }
}
