package com.james.status.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.DrawableRes;
import android.support.annotation.Nullable;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.support.v4.content.ContextCompat;

import java.util.Arrays;

public class IconStyleData implements Parcelable {

    public static final int TYPE_VECTOR = 0;
    public static final int TYPE_IMAGE = 1;
    public static final int TYPE_FILE = 2;

    public String name;
    public int type;
    public int[] resource = new int[0];
    public String[] path = new String[0];

    public IconStyleData(String name, int type, @DrawableRes int... resource) {
        this.name = name;
        this.type = type;
        this.resource = resource;
    }

    public IconStyleData(String name, String... path) {
        this.name = name;
        type = TYPE_FILE;
        this.path = path;
    }

    protected IconStyleData(Parcel in) {
        name = in.readString();
        resource = in.createIntArray();
        path = in.createStringArray();
    }

    public int getSize() {
        if (type == TYPE_FILE) return path.length;
        else return resource.length;
    }

    @Nullable
    public Drawable getDrawable(Context context, int value) {
        if (value < 0 || value >= getSize()) return null;
        switch (type) {
            case TYPE_VECTOR:
                return VectorDrawableCompat.create(context.getResources(), resource[value], context.getTheme());
            case TYPE_IMAGE:
                return ContextCompat.getDrawable(context, resource[value]);
            case TYPE_FILE:
                try {
                    return Drawable.createFromPath(path[value]);
                } catch (OutOfMemoryError e) {
                    e.printStackTrace();
                    return null;
                }
            default:
                return null;
        }
    }

    public void writeToSharedPreferences(SharedPreferences.Editor editor, String prefix) {
        if (type == TYPE_FILE) {
            editor.putInt(prefix + name + "-length", getSize());
            for (int i = 0; i < getSize(); i++) {
                editor.putString(prefix + name + "-" + i, path[i]);
            }
        }
    }

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
}
