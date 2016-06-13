package org.dolphin.secret.permission;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.dolphin.lib.util.ValueUtil;

/**
 * Created by hanyanan on 2016/6/13.
 */
public final class PermissionSpec implements Parcelable {
    final String permission;
    final int minVersionCode;

    PermissionSpec(String permission, int minVersionCode) {
        this.permission = permission;
        this.minVersionCode = minVersionCode;
    }

    PermissionSpec(String permission) {
        this.permission = permission;
        this.minVersionCode = -1;
    }

    private PermissionSpec(Parcel in) {
        permission = in.readString();
        minVersionCode = in.readInt();
    }


    public boolean isSupportOnDevice() {
        return Build.VERSION.SDK_INT >= minVersionCode;
    }

    @Override
    public int hashCode() {
        return permission.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }

        return ValueUtil.isEquals(this.permission, ((PermissionSpec) o).permission);
    }

    @Override
    public String toString() {
        return "[" + permission + ", " + minVersionCode + "]";
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(permission);
        dest.writeInt(minVersionCode);
    }


    public static final Creator<PermissionSpec> CREATOR = new Creator<PermissionSpec>() {
        @Override
        public PermissionSpec createFromParcel(Parcel in) {
            return new PermissionSpec(in);
        }

        @Override
        public PermissionSpec[] newArray(int size) {
            return new PermissionSpec[size];
        }
    };
}