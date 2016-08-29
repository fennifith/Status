package com.james.status.data;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;

public class ActivityColorData implements Parcelable {

    public String label, packageName, name;
    @Nullable
    public Integer color, cachedColor;

    public ActivityColorData(PackageManager manager, ActivityInfo info) {
        label = info.loadLabel(manager).toString();
        packageName = info.applicationInfo.packageName;
        name = info.name;
        color = null;
        cachedColor = null;
    }

    public ActivityColorData(PackageManager manager, ApplicationInfo info) {
        label = info.loadLabel(manager).toString();
        packageName = info.packageName;
        name = info.name;
        color = null;
        cachedColor = null;
    }

    public ComponentName getComponentName() {
        return new ComponentName(packageName, name != null ? name : packageName);
    }

    protected ActivityColorData(Parcel in) {
        label = in.readString();
        packageName = in.readString();
        color = in.readInt();
        cachedColor = in.readInt();
    }

    public static final Creator<ActivityColorData> CREATOR = new Creator<ActivityColorData>() {
        @Override
        public ActivityColorData createFromParcel(Parcel in) {
            return new ActivityColorData(in);
        }

        @Override
        public ActivityColorData[] newArray(int size) {
            return new ActivityColorData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(label);
        parcel.writeString(packageName);
        if (color != null) parcel.writeInt(color);
        if (cachedColor != null) parcel.writeInt(cachedColor);
    }
}
