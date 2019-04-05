/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.data.preference;

import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.view.View;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;

public class ListPreferenceData extends BasePreferenceData<Integer> {

    private int preference;
    private List<ListPreference> items;

    private ListPreference selectedPreference;

    public ListPreferenceData(Context context, Identifier<Integer> identifier, OnPreferenceChangeListener<Integer> listener, ListPreference... items) {
        super(context, identifier, listener);

        this.items = new ArrayList<>();
        this.items.addAll(Arrays.asList(items));

        preference = identifier.getPreferenceValue(context);
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
                .setSingleChoiceItems(array, items.indexOf(selectedPreference), (dialog, which) -> selectedPreference = items.get(which))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> {
                    if (selectedPreference != null) {
                        ListPreferenceData.this.preference = selectedPreference.id;

                        getIdentifier().setPreferenceValue(getContext(), selectedPreference.id);
                        onPreferenceChange(selectedPreference.id);
                        selectedPreference = null;
                    }
                })
                .setNegativeButton(android.R.string.cancel, (dialog, which) -> selectedPreference = null)
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
