package org.dolphin.secret.permission;

import android.os.Build;
import android.os.Parcel;
import android.os.Parcelable;

import org.dolphin.lib.util.ValueUtil;

/**
 * Created by hanyanan on 2016/6/13.
 */
public final class PermissionSpec implements Parcelable {
    private static final String DEFAULT_DESCRIPTION = "";
    final String permission;
    final int minVersionCode;
    final String description;

    PermissionSpec(String permission, String description, int minVersionCode) {
        this.permission = permission;
        this.description = description;
        this.minVersionCode = minVersionCode;
    }

    PermissionSpec(String permission, int minVersionCode) {
        this.permission = permission;
        this.description = DEFAULT_DESCRIPTION;
        this.minVersionCode = minVersionCode;
    }

    PermissionSpec(String permission) {
        this.permission = permission;
        this.description = DEFAULT_DESCRIPTION;
        this.minVersionCode = -1;
    }

    protected PermissionSpec(Parcel in) {
        permission = in.readString();
        minVersionCode = in.readInt();
        description = in.readString();
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
        dest.writeString(description);
    }
}