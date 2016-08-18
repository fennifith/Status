package com.james.status.data;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class AppColorData implements Parcelable {

    public String name, packageName;
    @Nullable
    public Integer color, cachedColor;

    public AppColorData(PackageManager manager, ResolveInfo info) {
        name = info.loadLabel(manager).toString();
        packageName = info.activityInfo.applicationInfo.packageName;
        color = null;
        cachedColor = null;
    }

    protected AppColorData(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        color = in.readInt();
        cachedColor = in.readInt();
    }

    public static final Creator<AppColorData> CREATOR = new Creator<AppColorData>() {
        @Override
        public AppColorData createFromParcel(Parcel in) {
            return new AppColorData(in);
        }

        @Override
        public AppColorData[] newArray(int size) {
            return new AppColorData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeString(packageName);
        if (color != null) parcel.writeInt(color);
        if (cachedColor != null) parcel.writeInt(cachedColor);
    }
}
