package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.ListPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class ListPreferenceData extends PreferenceData<Integer> {

    private int preference;
    private List<ListPreference> items;

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
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_text, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ((TextView) holder.v.findViewById(R.id.title)).setText(getIdentifier().getTitle());

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ListPreference listPreference = getListPreference(preference);
                if (listPreference == null) return;

                Dialog dialog = new ListPickerDialog(getContext(), items).setPreference(listPreference).setListener(new PreferenceDialog.OnPreferenceListener<ListPreference>() {
                    @Override
                    public void onPreference(PreferenceDialog dialog, ListPreference preference) {
                        ListPreferenceData.this.preference = preference.id;

                        PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                        if (identifier != null)
                            PreferenceUtils.putPreference(getContext(), identifier, preference.id);

                        onPreferenceChange(preference.id);
                    }

                    @Override
                    public void onCancel(PreferenceDialog dialog) {
                    }
                });
                dialog.setTitle(getIdentifier().getTitle());
                dialog.show();
            }
        });
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
