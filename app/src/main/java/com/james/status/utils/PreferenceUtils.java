package com.james.status.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import com.google.gson.Gson;

import java.util.Set;

public class PreferenceUtils {

    public enum PreferenceIdentifier {
        STATUS_ENABLED,
        STATUS_CLOCK_AMPM,
        STATUS_BATTERY_PERCENT,
        STATUS_COLOR_AUTO,
        STATUS_COLOR,
        STATUS_COLOR_APPS,
        STATUS_DARK_ICONS,
        STATUS_LOCKSCREEN_EXPAND,
        BATTERY_ICON_STYLE,
        NETWORK_ICON_STYLE,
        WIFI_ICON_STYLE,
        GPS_ICON_STYLE,
        BLUETOOTH_ICON_STYLE,
        AIRPLANE_MODE_ICON_STYLE,
        ALARM_ICON_STYLE
    }

    @Nullable
    public static Object getPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getAll().get(identifier.toString());
        else
            return null;
    }

    @Nullable
    public static <T> T getObjectPreference(Context context, PreferenceIdentifier identifier, Class<T> classOfT) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return new Gson().fromJson(prefs.getString(identifier.toString(), null), classOfT);
        else
            return null;
    }

    @Nullable
    public static Boolean getBooleanPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getBoolean(identifier.toString(), false);
        else
            return null;
    }

    @Nullable
    public static Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getInt(identifier.toString(), 0);
        else
            return null;
    }

    @Nullable
    public static int[] getIntegerArrayPreference(Context context, PreferenceIdentifier identifier, int length) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        int[] value = new int[length];
        for (int i = 0; i < value.length; i++) {
            if (prefs.contains(identifier.toString() + i))
                value[i] = prefs.getInt(identifier.toString() + i, 0);
            else return null;
        }

        return value;
    }

    @Nullable
    public static String getStringPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getString(identifier.toString(), null);
        else
            return null;
    }

    @Nullable
    public static Float getFloatPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getFloat(identifier.toString(), 0f);
        else
            return null;
    }

    @Nullable
    public static Long getLongPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getLong(identifier.toString(), 0);
        else
            return null;
    }

    @Nullable
    public static Set<String> getStringSetPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getStringSet(identifier.toString(), null);
        else
            return null;
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, Object object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(identifier.toString(), new Gson().toJson(object)).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int[] object) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        for (int i = 0; i < object.length; i++) {
            prefs.edit().putInt(identifier.toString() + i, object[i]).apply();
        }
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, String object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putString(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, float object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putFloat(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, long object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putLong(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, Set<String> object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putStringSet(identifier.toString(), object).apply();
    }
}
