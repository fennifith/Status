package com.james.status.data;

import android.os.Parcel;
import android.os.Parcelable;

public class FaqData implements Parcelable {

    public String name, content;

    public FaqData(String name, String content) {
        this.name = name;
        this.content = content;
    }

    protected FaqData(Parcel in) {
        name = in.readString();
        content = in.readString();
    }

    public static final Creator<FaqData> CREATOR = new Creator<FaqData>() {
        @Override
        public FaqData createFromParcel(Parcel in) {
            return new FaqData(in);
        }

        @Override
        public FaqData[] newArray(int size) {
            return new FaqData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(name);
        dest.writeString(content);
    }
}
