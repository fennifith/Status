package com.james.status.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Parcel;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;

import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;

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
        if (packageInfo.activities != null) {
            for (ActivityInfo activityInfo : packageInfo.activities) {
                activities.add(new ActivityData(manager, activityInfo));
            }
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

    @ColorInt
    public int getColor(Context context) {
        Integer color = getIntegerPreference(context, PreferenceIdentifier.COLOR);
        if (color == null) color = getDefaultColor(context);

        return color;
    }

    @ColorInt
    public int getDefaultColor(Context context) {
        Integer color = null;

        Boolean isAuto = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isAuto == null || isAuto)
            color = ColorUtils.getPrimaryColor(context, getComponentName());
        if (color == null)
            color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;

        return color;
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
        if (prefs.contains(getIdentifierString(identifier))) {
            try {
                return prefs.getBoolean(getIdentifierString(identifier), false);
            } catch (ClassCastException e) {
                return null;
            }
        }
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

    public void clearPreferences(Context context) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = prefs.edit();
        for (String key : prefs.getAll().keySet()) {
            if (key.contains("/" + packageName + "/")) editor.remove(key);
        }
        editor.apply();
    }

    private String getIdentifierString(PreferenceIdentifier identifier) {
        return identifier.toString() + "/" + packageName + "/" + name;
    }

    public enum PreferenceIdentifier {
        NOTIFICATIONS,
        COLOR,
        FULLSCREEN,
        CACHE_COLOR,
        CACHE_VERSION
    }

    public static class ActivityData implements Parcelable {

        public String label, packageName, name;
        public int version = 0;
        private Map<String, Object> tags;

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
            dest.writeInt(version);
        }

        public ComponentName getComponentName() {
            return new ComponentName(packageName, name != null ? name : packageName);
        }

        @ColorInt
        public int getColor(Context context) {
            Integer color = getIntegerPreference(context, PreferenceIdentifier.COLOR);

            Integer cacheVersion = getIntegerPreference(context, AppData.PreferenceIdentifier.CACHE_VERSION);
            if (cacheVersion != null && cacheVersion == version && color == null)
                color = getIntegerPreference(context, PreferenceIdentifier.CACHE_COLOR);

            if (color == null) color = getDefaultColor(context);

            return color;
        }

        @ColorInt
        public int getDefaultColor(Context context) {
            Integer color = null;

            Boolean isAuto = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
            if (isAuto == null || isAuto)
                color = ColorUtils.getPrimaryColor(context, getComponentName());
            if (color == null)
                color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (color == null) color = Color.BLACK;

            return color;
        }

        @Nullable
        public Boolean getBooleanPreference(Context context, PreferenceIdentifier identifier) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(getIdentifierString(identifier))) {
                try {
                    return prefs.getBoolean(getIdentifierString(identifier), false);
                } catch (ClassCastException e) {
                    return null;
                }
            }
            else
                return null;
        }

        @Nullable
        public Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
            SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
            if (prefs.contains(getIdentifierString(identifier))) {
                try {
                    return prefs.getInt(getIdentifierString(identifier), 0);
                } catch (ClassCastException e) {
                    return null;
                }
            }
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
