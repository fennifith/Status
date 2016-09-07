package com.james.status.utils;

import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

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
        STATUS_ICON_PADDING,
        STATUS_ICON_SCALE,
        STATUS_HEADS_UP_DURATION,
        SHOW_NOTIFICATIONS,
        SHOW_TUTORIAL
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
        if (prefs.contains(identifier.toString()))
            return prefs.getBoolean(identifier.toString(), false);
        else
            return null;
    }

    @Nullable
    public static Integer getIntegerPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getInt(identifier.toString(), 0);
        else
            return null;
    }

    @Nullable
    public static int[] getResourceIntPreference(Context context, PreferenceIdentifier identifier, String resourceType) {
        if (context == null || identifier == null || resourceType == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        if (prefs.contains(identifier.toString() + "-length")) {
            int length = prefs.getInt(identifier.toString() + "-length", 0);
            int[] value = new int[length];

            for (int i = 0; i < length; i++) {
                if (prefs.contains(identifier.toString() + "-" + i))
                    value[i] = resources.getIdentifier(prefs.getString(identifier.toString() + "-" + i, null), resourceType, context.getPackageName());
                else return null;
            }

            return value;
        } else return null;
    }

    @Nullable
    public static String getStringPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getString(identifier.toString(), null);
        else
            return null;
    }

    @Nullable
    public static Float getFloatPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getFloat(identifier.toString(), 0f);
        else
            return null;
    }

    @Nullable
    public static Long getLongPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        if (prefs.contains(identifier.toString()))
            return prefs.getLong(identifier.toString(), 0);
        else
            return null;
    }

    @Nullable
    public static List<String> getStringListPreference(Context context, PreferenceIdentifier identifier) {
        if (context == null || identifier == null) return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(identifier.toString() + "-length")) {
            int length = prefs.getInt(identifier.toString() + "-length", 0);
            List<String> value = new ArrayList<>();

            for (int i = 0; i < length; i++) {
                if (prefs.contains(identifier.toString() + "-" + i))
                    value.add(prefs.getString(identifier.toString() + "-" + i, null));
                else return null;
            }

            return value;
        } else return null;
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, boolean object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(identifier.toString(), object).apply();
    }

    public static void putPreference(Context context, PreferenceIdentifier identifier, int object) {
        PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(identifier.toString(), object).apply();
    }

    public static void putResourcePreference(Context context, PreferenceIdentifier identifier, int[] object) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Resources resources = context.getResources();

        prefs.edit().putInt(identifier.toString() + "-length", object.length).apply();

        for (int i = 0; i < object.length; i++) {
            prefs.edit().putString(identifier.toString() + "-" + i, resources.getResourceEntryName(object[i])).apply();
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

    public static void putPreference(Context context, PreferenceIdentifier identifier, List<String> object) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        prefs.edit().putInt(identifier.toString() + "-length", object.size()).apply();

        for (int i = 0; i < object.size(); i++) {
            prefs.edit().putString(identifier.toString() + "-" + i, object.get(i)).apply();
        }
    }
}
