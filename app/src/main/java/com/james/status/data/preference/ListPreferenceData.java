package com.james.status.data.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.view.View;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListPreferenceData extends PreferenceData<Integer> {

    private int preference;
    private List<ListPreference> items;

    private ListPreference selectedPreference;

    public ListPreferenceData(Context context, Identifier identifier, OnPreferenceChangeListener<Integer> listener, int defaultItem, ListPreference... items) {
        super(context, identifier, listener);

        this.items = new ArrayList<>();
        this.items.addAll(Arrays.asList(items));

        PreferenceUtils.PreferenceIdentifier preferenceIdentifier = identifier.getPreference();
        Integer integer = null;

        if (preferenceIdentifier != null)
            integer = PreferenceUtils.getIntegerPreference(context, preferenceIdentifier);

        if (integer != null) preference = integer;
        else preference = defaultItem;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onClick(View v) {
        selectedPreference = getListPreference(preference);
        if (selectedPreference == null) return;

        CharSequence[] array = new CharSequence[items.size()];
        for (int i = 0; i < items.size(); i++) {
            array[i] = items.get(i).name;
        }

        new AlertDialog.Builder(getContext())
                .setTitle(getIdentifier().getTitle())
                .setSingleChoiceItems(array, items.indexOf(selectedPreference), new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPreference = items.get(which);
                    }
                })
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        if (selectedPreference != null) {
                            ListPreferenceData.this.preference = selectedPreference.id;

                            PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                            if (identifier != null)
                                PreferenceUtils.putPreference(getContext(), identifier, selectedPreference.id);

                            onPreferenceChange(selectedPreference.id);
                            selectedPreference = null;
                        }
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPreference = null;
                    }
                })
                .create()
                .show();
    }

    @Nullable
    private ListPreference getListPreference(int id) {
        for (ListPreference preference : items) {
            if (preference.id == id) return preference;
        }

        return null;
    }

    public static class ListPreference implements Parcelable {

        public String name;
        public int id;

        public ListPreference(String name, int id) {
            this.name = name;
            this.id = id;
        }

        protected ListPreference(Parcel in) {
            name = in.readString();
            id = in.readInt();
        }

        public static final Creator<ListPreference> CREATOR = new Creator<ListPreference>() {
            @Override
            public ListPreference createFromParcel(Parcel in) {
                return new ListPreference(in);
            }

            @Override
            public ListPreference[] newArray(int size) {
                return new ListPreference[size];
            }
        };

        @Override
        public int describeContents() {
            return 0;
        }

        @Override
        public void writeToParcel(Parcel dest, int flags) {
            dest.writeString(name);
            dest.writeInt(id);
        }
    }
}
