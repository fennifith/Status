/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.data;

import android.content.ComponentName;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;

import java.util.ArrayList;
import java.util.List;

public class AppData implements Parcelable {

    public String label, packageName, name;
    public List<ActivityData> activities;

    public AppData(PackageManager manager, ApplicationInfo info, PackageInfo packageInfo) {
        label = info.loadLabel(manager).toString();
        packageName = info.packageName;
        name = info.name;

        activities = new ArrayList<>();
        if (packageInfo.activities != null) {
            for (ActivityInfo activityInfo : packageInfo.activities) {
                activities.add(new ActivityData(manager, activityInfo));
            }
        }
    }

    protected AppData(Parcel in) {
        label = in.readString();
        packageName = in.readString();
        name = in.readString();

        activities = new ArrayList<>();
        in.readTypedList(activities, ActivityData.CREATOR);
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
    public void writeToParcel(Parcel parcel, int flags) {
        parcel.writeString(label);
        parcel.writeString(packageName);
        parcel.writeString(name);
        parcel.writeTypedList(activities);
    }

    public static class ActivityData implements Parcelable {

        public String label, packageName, name;
        public int version = 0;

        public ActivityData(PackageManager manager, ActivityInfo info) {
            label = info.loadLabel(manager).toString();
            packageName = info.applicationInfo.packageName;
            name = info.name;

            try {
                version = manager.getPackageInfo(info.packageName, PackageManager.GET_META_DATA).versionCode;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        protected ActivityData(Parcel in) {
            label = in.readString();
            packageName = in.readString();
            name = in.readString();
            version = in.readInt();
        }

        public static final Creator<ActivityData> CREATOR = new Creator<ActivityData>() {
            @Override
            public ActivityData createFromParcel(Parcel in) {
                return new ActivityData(in);
            }

            @Override
            public ActivityData[] newArray(int size) {
                return new ActivityData[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(label);
            dest.writeString(packageName);
            dest.writeString(name);
            dest.writeInt(version);
        }

        public ComponentName getComponentName() {
            return new ComponentName(packageName, name != null ? name : packageName);
        }
    }
}
