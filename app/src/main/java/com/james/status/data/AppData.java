package com.james.status.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppData implements Parcelable {

    public String label, packageName, name;
    public List<ActivityData> activities;
    private Map<String, Object> tags;

    public AppData(PackageManager manager, ApplicationInfo info, PackageInfo packageInfo) {
        label = info.loadLabel(manager).toString();
        packageName = info.packageName;
        name = info.name;

        activities = new ArrayList<>();
        for (ActivityInfo activityInfo : packageInfo.activities) {
            activities.add(new ActivityData(manager, activityInfo));
        }
    }

    public ComponentName getComponentName() {
        return new ComponentName(packageName, name != null ? name : packageName);
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

    public void setTag(String key, Object value) {
        if (tags == null) tags = new HashMap<>();
        tags.put(key, value);
    }

    @Nullable
    public Object getTag(String key) {
        if (tags == null) tags = new HashMap<>();
        return tags.get(key);
    }

    @Nullable
    public Boolean getBooleanPreference(Context context, PreferenceIdentifier identifier) {
        Boolean bool = null;
        for (ActivityData activity : activities) {
            Boolean activityBool = activity.getBooleanPreference(context, identifier);
            if (activityBool == null || (bool != null && activityBool != bool)) return null;
            else bool = activityBool;
        }

        return bool;
    }

    @Nullable
    public Boolean getSpecificBooleanPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(getIdentifierString(identifier)))
            return prefs.getBoolean(getIdentifierString(identifier), false);
        else
            return null;
    }

    @Nullable
    public Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
        Integer integer = null;
        for (ActivityData activity : activities) {
            Integer activityInteger = activity.getIntegerPreference(context, identifier);
            if (activityInteger == null || (integer != null && !activityInteger.equals(integer)))
                return null;
            else integer = activityInteger;
        }

        return integer;
    }

    public void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        for (ActivityData activity : activities) {
            activity.putPreference(context, identifier, object);
        }
    }

    public void putSpecificPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getIdentifierString(identifier), object).apply();
    }

    public void putPreference(Context context, PreferenceIdentifier identifier, int object) {
        for (ActivityData activity : activities) {
            activity.putPreference(context, identifier, object);
        }
    }

    private String getIdentifierString(PreferenceIdentifier identifier) {
        return identifier.toString() + "/" + packageName + "/" + name;
    }

    public enum PreferenceIdentifier {
        NOTIFICATIONS,
        COLOR,
        FULLSCREEN
    }

    public static class ActivityData implements Parcelable {

        public String label, packageName, name;
        private Map<String, Object> tags;

        public ActivityData(PackageManager manager, ActivityInfo info) {
            label = info.loadLabel(manager).toString();
            packageName = info.applicationInfo.packageName;
            name = info.name;
        }

        protected ActivityData(Parcel in) {
            label = in.readString();
            packageName = in.readString();
            name = in.readString();
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

        public void setTag(String key, Object value) {
            if (tags == null) tags = new HashMap<>();
            tags.put(key, value);
        }

        @Nullable
        public Object getTag(String key) {
            if (tags == null) tags = new HashMap<>();
            return tags.get(key);
        }

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(label);
            dest.writeString(packageName);
            dest.writeString(name);
        }

        public ComponentName getComponentName() {
            return new ComponentName(packageName, name != null ? name : packageName);
        }

        @Nullable
        public Boolean getBooleanPreference(Context context, PreferenceIdentifier identifier) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(getIdentifierString(identifier)))
                return prefs.getBoolean(getIdentifierString(identifier), false);
            else
                return null;
        }

        @Nullable
        public Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(getIdentifierString(identifier)))
                return prefs.getInt(getIdentifierString(identifier), 0);
            else
                return null;
        }

        public void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(getIdentifierString(identifier), object).apply();
        }

        public void putPreference(Context context, PreferenceIdentifier identifier, int object) {
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(getIdentifierString(identifier), object).apply();
        }

        private String getIdentifierString(PreferenceIdentifier identifier) {
            return identifier.toString() + "/" + packageName + "/" + name;
        }
    }
}
