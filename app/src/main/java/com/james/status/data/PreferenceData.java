package com.james.status.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.Typeface;
import android.os.Build;
import android.os.Environment;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
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
    STATUS_ICON_TEXT_COLOR(Color.WHITE),
    STATUS_DARK_ICON_COLOR(Color.BLACK),
    STATUS_DARK_ICON_TEXT_COLOR(Color.BLACK),
    STATUS_LIGHT_ICON_COLOR(Color.WHITE),
    STATUS_LIGHT_ICON_TEXT_COLOR(Color.WHITE),
    STATUS_DARK_ICONS(true),
    STATUS_TINTED_ICONS(false),
    STATUS_PREVENT_ICON_OVERLAP(false),
    STATUS_BUMP_MODE(false),
    STATUS_HEADS_UP_DURATION(10),
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
    STATUS_DEBUG(BuildConfig.DEBUG),
    ICON_VISIBILITY("%1$s/VISIBILITY", true),
    ICON_POSITION("%1$s/POSITION", 0),
    ICON_GRAVITY("%1$s/GRAVITY", 0),
    ICON_TEXT_VISIBILITY("%1$s/TEXT_VISIBILITY", false),
    ICON_TEXT_FORMAT("%1$s/TEXT_FORMAT", "h:mm a"),
    ICON_TEXT_SIZE("%1$s/TEXT_SIZE", 14),
    ICON_TEXT_COLOR("%1$s/TEXT_COLOR", Color.WHITE),
    ICON_TEXT_TYPEFACE("%1$s/TEXT_TYPEFACE", ""),
    ICON_TEXT_EFFECT("%1$s/TEXT_EFFECT", Typeface.BOLD),
    ICON_ICON_VISIBILITY("%1$s/ICON_VISIBILITY", true),
    ICON_ICON_COLOR("%1$s/ICON_COLOR", Color.WHITE),
    ICON_ICON_STYLE("%1$s/ICON_STYLE", ""),
    ICON_ICON_STYLE_NAMES("%1$s/ICON_STYLE_NAMES", new String[]{}),
    ICON_ICON_PADDING("%1$s/ICON_PADDING", 2),
    ICON_ICON_SCALE("%1$s/ICON_SCALE", 18);

    private String name;
    private Object defaultValue;

    PreferenceData(Object value) {
        name = name();
        defaultValue = value;
    }

    PreferenceData(String name, Object value) {
        this.name = name;
        defaultValue = value;
    }

    public String getName(@Nullable String... args) {
        if (args != null && args.length > 0)
            return String.format(name, (Object[]) args);
        else return name;
    }

    public <T> T getDefaultValue() {
        try {
            return (T) defaultValue;
        } catch (ClassCastException e) {
            throw new TypeMismatchException(this);
        }
    }

    public <T> T getValue(Context context) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), (String[]) null);
    }

    public <T> T getValue(Context context, @Nullable T defaultValue) {
        return getSpecificOverriddenValue(context, defaultValue, (String[]) null);
    }

    public <T> T getSpecificValue(Context context, @Nullable String... args) {
        return getSpecificOverriddenValue(context, (T) getDefaultValue(), args);
    }

    public <T> T getSpecificOverriddenValue(Context context, @Nullable T defaultValue, @Nullable String... args) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        String name = getName(args);
        T type = defaultValue != null ? defaultValue : (T) getDefaultValue();

        if (type instanceof Object[] && prefs.contains(name + "-length")) {
            try {
                int length = prefs.getInt(name + "-length", 0);

                Object[] array;
                if (type instanceof Boolean[])
                    array = new Boolean[length];
                else if (type instanceof Integer[])
                    array = new Integer[length];
                else if (type instanceof String[])
                    array = new String[length];
                else throw new TypeMismatchException(this);

                for (int i = 0; i < array.length; i++) {
                    if (array instanceof Boolean[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getBoolean(name + "-" + i, false) : null;
                    else if (array instanceof Integer[])
                        array[i] = prefs.contains(name + "-" + i) ? prefs.getInt(name + "-" + i, 0) : null;
                    else if (array instanceof String[])
                        array[i] = prefs.getString(name + "-" + i, "");
                    else throw new TypeMismatchException(this);
                }

                return (T) array;
            } catch (ClassCastException e) {
                throw new TypeMismatchException(this, type.getClass());
            }
        } else if (prefs.contains(name)) {
            try {
                if (type instanceof Boolean)
                    return (T) new Boolean(prefs.getBoolean(name, (Boolean) defaultValue));
                else if (type instanceof Integer)
                    return (T) new Integer(prefs.getInt(name, (Integer) defaultValue));
                else if (type instanceof String)
                    return (T) prefs.getString(name, (String) defaultValue);
            } catch (ClassCastException e) {
                throw new TypeMismatchException(this, type.getClass());
            }
        }

        return defaultValue;
    }

    public <T> void setValue(Context context, @Nullable T value) {
        setValue(context, value, (String[]) null);
    }

    public <T> void setValue(Context context, @Nullable T value, @Nullable String... args) {
        SharedPreferences.Editor editor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        String name = getName(args);

        if (value == null)
            editor.remove(name + (defaultValue instanceof Object[] ? "-length" : ""));
        else if (value instanceof Object[]) {
            Object[] array = (Object[]) value;

            for (int i = 0; i < array.length; i++) {
                Object item = array[i];
                if (item instanceof Boolean)
                    editor.putBoolean(name + "-" + i, (boolean) item);
                else if (item instanceof Integer)
                    editor.putInt(name + "-" + i, (int) item);
                else if (item instanceof String)
                    editor.putString(name + "-" + i, (String) item);
                else throw new TypeMismatchException(this);
            }

            editor.putInt(name + "-length", array.length);
        } else {
            if (value instanceof Boolean)
                editor.putBoolean(name, (Boolean) value);
            else if (value instanceof Integer)
                editor.putInt(name, (Integer) value);
            else if (value instanceof String)
                editor.putString(name, (String) value);
            else throw new TypeMismatchException(this);
        }

        editor.apply();
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

        public TypeMismatchException(PreferenceData data) {
            this(data, null);
        }

        public TypeMismatchException(PreferenceData data, Class expectedType) {
            super("Wrong type used for \"" + data.name() + "\""
                    + (data.defaultValue != null ? ": expected " + data.defaultValue.getClass().getName()
                    + (expectedType != null ? ", got " + expectedType.getName() : "") : ""));
        }

    }

}
