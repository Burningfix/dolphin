package org.dolphin.secret.picker;

import android.os.Parcel;
import android.os.Parcelable;

/**
 * Created by hanyanan on 2016/7/20.
 */
public class AndroidFileInfo implements Parcelable {
    public static final Creator<AndroidFileInfo> CREATOR = new Creator<AndroidFileInfo>() {
        @Override
        public AndroidFileInfo createFromParcel(Parcel in) {
            return new AndroidFileInfo(in);
        }

        @Override
        public AndroidFileInfo[] newArray(int size) {
            return new AndroidFileInfo[size];
        }
    };
    public String id;
    public String name;
    public String path;
    public long size;
    public long lastModifyTime;
    public String mimeType;
    public boolean isFolder;
    public int width;
    public int height;

    public AndroidFileInfo() {

    }

    private AndroidFileInfo(Parcel in) {
        id = in.readString();
        name = in.readString();
        path = in.readString();
        size = in.readLong();
        lastModifyTime = in.readLong();
        mimeType = in.readString();
        isFolder = in.readByte() > 0;
        width = in.readInt();
        height = in.readInt();
    }

    @Override
    public int hashCode() {
        return id.hashCode();
    }

    @Override
    public boolean equals(Object o) {

        return super.equals(o);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(id);
        dest.writeString(name);
        dest.writeString(path);
        dest.writeLong(size);
        dest.writeLong(lastModifyTime);
        dest.writeString(mimeType);
        dest.writeByte((byte) (isFolder ? 1 : 0));
        dest.writeInt(width);
        dest.writeInt(height);
    }
}
