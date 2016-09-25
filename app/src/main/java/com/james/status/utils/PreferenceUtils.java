package com.james.status.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

public class PreferenceUtils {

    public enum PreferenceIdentifier {
        STATUS_ENABLED,
        STATUS_NOTIFICATIONS_COMPAT,
        STATUS_NOTIFICATIONS_HEADS_UP,
        STATUS_COLOR_AUTO,
        STATUS_COLOR,
        STATUS_HOME_TRANSPARENT,
        STATUS_COLORED_APPS_NOTIFICATIONS,
        STATUS_DARK_ICONS,
        STATUS_TINTED_ICONS,
        STATUS_LOCKSCREEN_EXPAND,
        STATUS_HEADS_UP_DURATION,
        STATUS_BACKGROUND_ANIMATIONS,
        STATUS_ICON_ANIMATIONS
    }

    @Nullable
    public static Object getPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getAll().get(identifier.toString());
        else
            return null;
    }

    @Nullable
    public static Boolean getBooleanPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString())) {
            try {
                return prefs.getBoolean(identifier.toString(), false);
            } catch (ClassCastException e) {
                return null;
            }
        } else return null;
    }

    @Nullable
    public static Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString())) {
            try {
                return prefs.getInt(identifier.toString(), 0);
            } catch (ClassCastException e) {
                return null;
            }
        } else return null;
    }

    @Nullable
    public static String getStringPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString())) {
            if (prefs.contains(identifier.toString())) {
                try {
                    return prefs.getString(identifier.toString(), null);
                } catch (ClassCastException e) {
                    return null;
                }
            } else return null;
        } else return null;
    }

    @Nullable
    public static Float getFloatPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString())) {
            if (prefs.contains(identifier.toString())) {
                try {
                    return prefs.getFloat(identifier.toString(), 0);
                } catch (ClassCastException e) {
                    return null;
                }
            } else return null;
        } else return null;
    }

    @Nullable
    public static Long getLongPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString())) {
            if (prefs.contains(identifier.toString())) {
                try {
                    return prefs.getLong(identifier.toString(), 0);
                } catch (ClassCastException e) {
                    return null;
                }
            } else return null;
        } else return null;
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(identifier.toString(), object).apply();
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
}
