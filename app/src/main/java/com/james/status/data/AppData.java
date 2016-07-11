package com.james.status.data;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class AppData implements Parcelable {

    public String name, packageName;
    @Nullable
    public Integer color;

    public AppData(PackageManager manager, ResolveInfo info) {
        name = info.loadLabel(manager).toString();
        packageName = info.activityInfo.applicationInfo.packageName;
        color = null;
    }

    protected AppData(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        color = in.readInt();
    }

    public static final Creator<AppData> CREATOR = new Creator<AppData>() {
        @Override
        public AppData createFromParcel(Parcel in) {
            return new AppData(in);
        }

        @Override
        public AppData[] newArray(int size) {
            return new AppData[size];
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
    }
}
