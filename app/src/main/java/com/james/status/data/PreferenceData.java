package com.james.status.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.widget.Toast;

import com.google.gson.Gson;
import com.james.status.BuildConfig;
import com.james.status.services.StatusService;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.util.Map;
import java.util.Set;

public enum PreferenceData {

    STATUS_ENABLED(false),
    STATUS_NOTIFICATIONS_COMPAT(false),
    STATUS_NOTIFICATIONS_HEADS_UP(false),
    STATUS_COLOR_AUTO(true),
    STATUS_COLOR(Color.BLACK),
    STATUS_HOME_TRANSPARENT(true),
    STATUS_ICON_COLOR(Color.WHITE),
    STATUS_DARK_ICONS(true),
    STATUS_TINTED_ICONS(false),
    STATUS_PREVENT_ICON_OVERLAP(false),
    STATUS_BUMP_MODE(false),
    STATUS_HEADS_UP_DURATION(5),
    STATUS_BACKGROUND_ANIMATIONS(true),
    STATUS_ICON_ANIMATIONS(true),
    STATUS_HEADS_UP_LAYOUT(StatusService.HEADSUP_LAYOUT_PLAIN),
    STATUS_HIDE_ON_VOLUME(Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP),
    STATUS_PERSISTENT_NOTIFICATION(true),
    STATUS_IGNORE_PERMISSION_CHECKING(false),
    STATUS_TRANSPARENT_MODE(false),
    STATUS_BURNIN_PROTECTION(false),
    STATUS_SIDE_PADDING(0),
    STATUS_HEIGHT(0),
    STATUS_DEBUG(BuildConfig.DEBUG);

    public static final int TYPE_BOOLEAN = 0;
    public static final int TYPE_INT = 1;
    public static final int TYPE_STRING = 2;
    public static final int TYPE_UNKNOWN = -1;

    private String name;

    private Boolean defaultBooleanValue;
    private Integer defaultIntValue;
    private String defaultStringValue;

    PreferenceData(boolean value) {
        name = name();
        defaultBooleanValue = value;
    }

    PreferenceData(int value) {
        name = name();
        defaultIntValue = value;
    }

    PreferenceData(String value) {
        name = name();
        defaultStringValue = value;
    }

    PreferenceData(String name, boolean value) {
        this.name = name;
        defaultBooleanValue = value;
    }

    PreferenceData(String name, int value) {
        this.name = name;
        defaultIntValue = value;
    }

    PreferenceData(String name, String value) {
        this.name = name;
        defaultStringValue = value;
    }

    public int getType() {
        if (defaultBooleanValue != null)
            return TYPE_BOOLEAN;
        else if (defaultIntValue != null)
            return TYPE_INT;
        else if (defaultStringValue != null)
            return TYPE_STRING;
        else return TYPE_UNKNOWN;
    }

    public boolean getDefaultBoolean() {
        if (getType() == TYPE_BOOLEAN)
            return defaultBooleanValue;
        else throw new TypeMismatchException(this, TYPE_BOOLEAN);
    }

    public int getDefaultInt() {
        if (getType() == TYPE_INT)
            return defaultIntValue;
        else throw new TypeMismatchException(this, TYPE_INT);
    }

    public String getDefaultString() {
        if (getType() == TYPE_STRING)
            return defaultStringValue;
        else throw new TypeMismatchException(this, TYPE_STRING);
    }

    public boolean getBooleanValue(Context context) {
        return getBooleanValue(context, getDefaultBoolean());
    }

    public int getIntValue(Context context) {
        return getIntValue(context, getDefaultInt());
    }

    public String getStringValue(Context context) {
        return getStringValue(context, getDefaultString());
    }

    public boolean getBooleanValue(Context context, boolean defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(name)) {
            try {
                return prefs.getBoolean(name, defaultValue);
            } catch (ClassCastException ignored) {
            }
        }

        return defaultValue;
    }

    public int getIntValue(Context context, int defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(name)) {
            try {
                return prefs.getInt(name, defaultValue);
            } catch (ClassCastException ignored) {
            }
        }

        return defaultValue;
    }

    public String getStringValue(Context context, @NonNull String defaultValue) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);

        if (prefs.contains(name)) {
            try {
                return prefs.getString(name, defaultValue);
            } catch (ClassCastException ignored) {
            }
        }

        return defaultValue;
    }

    public void setValue(Context context, boolean value) {
        if (getType() == TYPE_BOOLEAN)
            PreferenceManager.getDefaultSharedPreferences(context).edit().putBoolean(name, value).apply();
        else throw new TypeMismatchException(this, TYPE_BOOLEAN);
    }

    public void setValue(Context context, int value) {
        if (getType() == TYPE_INT)
            PreferenceManager.getDefaultSharedPreferences(context).edit().putInt(name, value).apply();
        else throw new TypeMismatchException(this, TYPE_INT);
    }

    public void setValue(Context context, String value) {
        if (getType() == TYPE_STRING)
            PreferenceManager.getDefaultSharedPreferences(context).edit().putString(name, value).apply();
        else throw new TypeMismatchException(this, TYPE_STRING);
    }

    public static boolean toFile(Context context, File file) {
        Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(context).getAll();

        FileOutputStream stream = null;
        try {
            stream = new FileOutputStream(file);
            stream.write(new Gson().toJson(prefs).getBytes());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
        }

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        } else return false;

        return file.exists();
    }

    public static boolean fromFile(Context context, File file) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        byte[] bytes = new byte[(int) file.length()];

        FileInputStream stream = null;
        try {
            stream = new FileInputStream(file);
            stream.read(bytes);
        } catch (IOException e) {
            e.printStackTrace();
        }

        if (stream != null) {
            try {
                stream.close();
            } catch (IOException ignored) {
            }
        }

        String contents = new String(bytes);
        SharedPreferences.Editor editor = prefs.edit();
        try {
            Map<String, ?> map = new Gson().fromJson(contents, Map.class);
            for (String key : map.keySet()) {
                Object value = map.get(key);
                if (value instanceof Boolean)
                    editor.putBoolean(key, (Boolean) value);
                else if (value instanceof Float)
                    editor.putFloat(key, (Float) value);
                else if (value instanceof Integer)
                    editor.putInt(key, (Integer) value);
                else if (value instanceof Long)
                    editor.putLong(key, (Long) value);
                else if (value instanceof String)
                    editor.putString(key, (String) value);
                else if (value instanceof Set)
                    editor.putStringSet(key, (Set) value);
            }
        } catch (Exception e) {
            e.printStackTrace();
            Toast.makeText(context, e.getMessage(), Toast.LENGTH_SHORT).show();
            return false;
        }

        return editor.commit();
    }

    public static String getBackupsDir() {
        return Environment.getExternalStorageDirectory() + "/status/backups";
    }

    public static class TypeMismatchException extends RuntimeException {

        public TypeMismatchException(PreferenceData data, int expectedType) {
            super("Wrong type used for \"" + data.name() + "\": expected " + getTypeString(data.getType()) + ", got " + getTypeString(expectedType));
        }

        private static String getTypeString(int type) {
            switch (type) {
                case TYPE_BOOLEAN:
                    return "boolean";
                case TYPE_INT:
                    return "int";
                case TYPE_STRING:
                    return "string";
                default:
                    return "unknown";
            }
        }

    }
}
