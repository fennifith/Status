package com.james.status.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.Set;

public class PreferenceUtils {

    public enum PreferenceIdentifier {
        STATUS_ENABLED,
        STATUS_CLOCK_24H,
        STATUS_CLOCK_AMPM,
        STATUS_BATTERY_PERCENT,
        STATUS_COLOR_AUTO,
        STATUS_COLOR,
        STATUS_COLOR_APPS,
        STATUS_FULLSCREEN_APPS,
        STATUS_DARK_ICONS,
        STATUS_LOCKSCREEN_EXPAND,
        STATUS_ICON_PADDING,
        STATUS_ICON_SCALE,
        STYLE_BATTERY_ICON,
        STYLE_NETWORK_ICON,
        STYLE_WIFI_ICON,
        STYLE_GPS_ICON,
        STYLE_BLUETOOTH_ICON,
        STYLE_AIRPLANE_MODE_ICON,
        STYLE_ALARM_ICON,
        STYLE_RINGER_ICON,
        SHOW_NOTIFICATIONS,
        SHOW_CLOCK,
        SHOW_BATTERY_ICON,
        SHOW_NETWORK_ICON,
        SHOW_DATA,
        SHOW_WIFI_ICON,
        SHOW_GPS_ICON,
        SHOW_BLUETOOTH_ICON,
        SHOW_AIRPLANE_MODE_ICON,
        SHOW_ALARM_ICON,
        SHOW_RINGER_ICON
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
    public static int[] getIntegerArrayPreference(Context context, PreferenceIdentifier identifier) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(identifier.toString() + "-length")) {
            int length = prefs.getInt(identifier.toString() + "-length", 0);
            int[] value = new int[length];

            for (int i = 0; i < length; i++) {
                if (prefs.contains(identifier.toString() + "-" + i))
                    value[i] = prefs.getInt(identifier.toString() + "-" + i, 0);
                else return null;
            }

            return value;
        } else return null;
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

    public static void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int[] object) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putInt(identifier.toString() + "-length", object.length).apply();

        for (int i = 0; i < object.length; i++) {
            prefs.edit().putInt(identifier.toString() + "-" + i, object[i]).apply();
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
