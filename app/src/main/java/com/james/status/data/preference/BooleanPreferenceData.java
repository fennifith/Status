package com.james.status.data.preference;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.james.status.R;

import androidx.appcompat.widget.SwitchCompat;

public class BooleanPreferenceData extends BasePreferenceData<Boolean> {

    public boolean value;

    public BooleanPreferenceData(Context context, Identifier<Boolean> identifier, OnPreferenceChangeListener<Boolean> listener) {
        super(context, identifier, listener);
        value = identifier.getPreferenceValue(context);
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        SwitchCompat titleView = holder.v.findViewById(R.id.title);

        titleView.setOnCheckedChangeListener(null);
        titleView.setChecked(value);
        titleView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                value = b;

                getIdentifier().setPreferenceValue(getContext(), b);
                onPreferenceChange(b);
            }
        });
    }
}
