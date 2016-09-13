package org.dolphin.secret.core;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hanyanan on 2016/1/15.
 */
public class ObscureFileInfo implements Parcelable {
    public static final Creator<ObscureFileInfo> CREATOR = new Creator<ObscureFileInfo>() {
        @Override
        public ObscureFileInfo createFromParcel(Parcel in) {
            return new ObscureFileInfo(in);
        }

        @Override
        public ObscureFileInfo[] newArray(int size) {
            return new ObscureFileInfo[size];
        }
    };
    public String dom;
    public int softwareVersion;
    public int encodeVersion;
    public long originalFileLength;
    public long originalModifyTimeStamp;
    public String originalFileName;
    public String obscuredFileName;
    public String originalMimeType;
    public long encodeTime;
    public byte[] extraTag;
    Range originalFileHeaderRange;
    Range originalFileFooterRange;
    Range thumbnailRange;
    int transferSize;

    public ObscureFileInfo() {

    }

    protected ObscureFileInfo(Parcel in) {
        dom = in.readString();
        softwareVersion = in.readInt();
        encodeVersion = in.readInt();
        originalFileLength = in.readLong();
        originalModifyTimeStamp = in.readLong();
        originalFileName = in.readString();
        obscuredFileName = in.readString();
        originalMimeType = in.readString();
        originalFileHeaderRange = in.readParcelable(Range.class.getClassLoader());
        originalFileFooterRange = in.readParcelable(Range.class.getClassLoader());
        thumbnailRange = in.readParcelable(Range.class.getClassLoader());
        transferSize = in.readInt();
        encodeTime = in.readLong();
        extraTag = in.createByteArray();
    }

    @Override
    public int hashCode() {
        return originalFileName.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (o == this) {
            return true;
        }

        if (!ObscureFileInfo.class.isInstance(o)) {
            return false;
        }
        ObscureFileInfo other = (ObscureFileInfo) o;
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

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(dom);
        dest.writeInt(softwareVersion);
        dest.writeInt(encodeVersion);
        dest.writeLong(originalFileLength);
        dest.writeLong(originalModifyTimeStamp);
        dest.writeString(originalFileName);
        dest.writeString(obscuredFileName);
        dest.writeString(originalMimeType);
        dest.writeParcelable(originalFileHeaderRange, flags);
        dest.writeParcelable(originalFileFooterRange, flags);
        dest.writeParcelable(thumbnailRange, flags);
        dest.writeInt(transferSize);
        dest.writeLong(encodeTime);
        dest.writeByteArray(extraTag);
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
        sb.append("\tproguardFileName: ").append(obscuredFileName).append("\n");
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

    // 闭区间
    static class Range implements Parcelable {
        public static final Creator<Range> CREATOR = new Creator<Range>() {
            @Override
            public Range createFromParcel(Parcel in) {
                return new Range(in);
            }

            @Override
            public Range[] newArray(int size) {
                return new Range[size];
            }
        };
        public long offset;
        public int count;

        public Range() {

        }

        protected Range(Parcel in) {
            offset = in.readLong();
            count = in.readInt();
        }

        @Override
        public String toString() {
            return "[offset: " + offset + ", count: " + count + "]";
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeLong(offset);
            dest.writeInt(count);
        }
    }
}
