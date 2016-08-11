package com.james.status.data;

import android.os.Parcel;
import android.os.Parcelable;

import java.util.Arrays;

public class IconStyleData implements Parcelable {

    public String name;
    public int[] resource;

    public IconStyleData(String name, int... resource) {
        this.name = name;
        this.resource = resource;
    }

    protected IconStyleData(Parcel in) {
        name = in.readString();
        resource = in.createIntArray();
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
    }

    public boolean equals(IconStyleData data) {
        return super.equals(data) || (data != null && (Arrays.equals(data.resource, resource) || data.name.matches(name)));
    }
}
