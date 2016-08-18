package com.james.status.data;

import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class AppStatusData implements Parcelable {

    public String name, packageName;
    @Nullable
    public boolean isFullscreen;

    public AppStatusData(PackageManager manager, ResolveInfo info) {
        name = info.loadLabel(manager).toString();
        packageName = info.activityInfo.applicationInfo.packageName;
    }

    protected AppStatusData(Parcel in) {
        name = in.readString();
        packageName = in.readString();
        isFullscreen = in.readInt() == 1;
    }

    public static final Creator<AppStatusData> CREATOR = new Creator<AppStatusData>() {
        @Override
        public AppStatusData createFromParcel(Parcel in) {
            return new AppStatusData(in);
        }

        @Override
        public AppStatusData[] newArray(int size) {
            return new AppStatusData[size];
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
        parcel.writeInt(isFullscreen ? 1 : 0);
    }
}
