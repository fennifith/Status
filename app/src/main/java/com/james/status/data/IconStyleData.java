package com.james.status.data;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.SharedPreferences;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import com.james.status.utils.StaticUtils;

import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.DrawableRes;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.vectordrawable.graphics.drawable.VectorDrawableCompat;
import me.jfenn.androidutils.ImageUtils;

public class IconStyleData implements Parcelable {

    public static final int TYPE_VECTOR = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_FILE = 2;

    public String name;
    public int type;
    public int[] resource = new int[0];
    public String[] path = new String[0];
    private Map<Integer, Bitmap> icons;

    public IconStyleData(String name, int type, @DrawableRes int... resource) {
        this.name = name;
        this.type = type;
        this.resource = resource;
        icons = new HashMap<>();
    }

    public IconStyleData(String name, String... path) {
        this.name = name;
        type = TYPE_FILE;
        this.path = path;
        icons = new HashMap<>();
    }

    protected IconStyleData(Parcel in) {
        name = in.readString();
        resource = in.createIntArray();
        path = in.createStringArray();
        icons = new HashMap<>();
    }

    /**
     * Get the amount of icons in the style.
     *
     * @return An integer representing the amount of
     * icons in the style.
     */
    public int getSize() {
        if (type == TYPE_FILE) return path.length;
        else return resource.length;
    }

    /**
     * Get a drawable of the style at a particular index.
     *
     * @param context               The current application context.
     * @param value                 The index to obtain.
     * @return A created Drawable, or null if
     *                              something has gone horribly wrong.
     */
    @Nullable
    public Drawable getDrawable(Context context, int value) {
        if (value < 0 || value >= getSize()) return null;
        switch (type) {
            case TYPE_VECTOR:
                return VectorDrawableCompat.create(context.getResources(), resource[value], context.getTheme());
            case TYPE_IMAGE:
                return ContextCompat.getDrawable(context, resource[value]);
            case TYPE_FILE:
                String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
                if (!StaticUtils.isPermissionsGranted(context, permissions)) {
                    if (context instanceof Activity)
                        StaticUtils.requestPermissions((Activity) context, permissions);

                    return null;
                } else {
                    try {
                        return Drawable.createFromPath(path[value]);
                    } catch (OutOfMemoryError e) {
                        e.printStackTrace();
                        return null;
                    }
                }
            default:
                return null;
        }
    }

    /**
     * Get a bitmap from the style at a particular index. This will
     * also cache the created bitmaps to increase performance of future
     * calls.
     *
     * @param context               The current application context.
     * @param value                 The index to obtain.
     * @return A created bitmap, or null if something
     *                              has gone horribly wrong.
     */
    @Nullable
    public Bitmap getBitmap(Context context, int value) {
        if (icons.containsKey(value))
            return icons.get(value);
        else {
            Drawable drawable = getDrawable(context, value);
            if (drawable != null) {
                Bitmap bitmap = ImageUtils.drawableToBitmap(drawable);
                icons.put(value, bitmap);
                return bitmap;
            } else return null;
        }
    }

    /**
     * Write the icon style to a SharedPreferences instance.
     *
     * @param editor                A SharedPreferences instance.
     * @param prefix                The preferences prefix.
     */
    public void writeToSharedPreferences(SharedPreferences.Editor editor, String prefix) {
        if (type == TYPE_FILE) {
            editor.putInt(prefix + name + "-length", getSize());
            for (int i = 0; i < getSize(); i++) {
                editor.putString(prefix + name + "-" + i, path[i]);
            }
        }
    }

    /**
     * Instantiate an IconStyleData from an index in SharedPreferences, based on a name.
     *
     * @param prefs                 A SharedPreferences instance.
     * @param prefix                The preferences prefix.
     * @param name                  The name of the style.
     * @return The created IconStyleData, or null if not found.
     */
    @Nullable
    public static IconStyleData fromSharedPreferences(SharedPreferences prefs, String prefix, String name) {
        if (prefs.contains(prefix + name + "-length")) {
            String[] path = new String[prefs.getInt(prefix + name + "-length", 0)];
            for (int i = 0; i < path.length; i++) {
                path[i] = prefs.getString(prefix + name + "-" + i, null);
            }

            return new IconStyleData(name, path);
        } else return null;
    }

    public static final Creator<IconStyleData> CREATOR = new Creator<IconStyleData>() {
        @Override
        public IconStyleData createFromParcel(Parcel in) {
            return new IconStyleData(in);
        }

        @Override
        public IconStyleData[] newArray(int size) {
            return new IconStyleData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel parcel, int i) {
        parcel.writeString(name);
        parcel.writeIntArray(resource);
        parcel.writeStringArray(path);
    }

    public boolean equals(IconStyleData data) {
        return super.equals(data) || (data != null && ((Arrays.equals(data.resource, resource) && resource.length > 0) || (Arrays.equals(data.path, path) && path.length > 0) || data.name.matches(name)));
    }

    /**
     * Instantiate an icon style from the app resources using a set of strings.
     * If a resource is not found, this will return null. This is particularly
     * useful for adding styles that may not necessarily be included in the
     * published source code of the application; resources that may be under
     * a different license will only be used if they are available.
     *
     * @param name                  The user-facing name of the style.
     * @param type                  The type of icons the style uses, e.g. vector assets.
     * @param context               The current application context.
     * @param resourceNames         The names of the resources.
     * @return The created IconStyleData, or null.
     */
    @Nullable
    public static IconStyleData fromResource(String name, int type, Context context, String... resourceNames) {
        int[] resourceInts = new int[resourceNames.length];
        Resources resources = context.getResources();
        String packageName = context.getPackageName();
        for (int i = 0; i < resourceNames.length; i++) {
            int resource = resources.getIdentifier(resourceNames[i], "drawable", packageName);
            if (resource != 0)
                resourceInts[i] = resource;
            else return null;
        }

        return new IconStyleData(name, type, resourceInts);
    }
}
