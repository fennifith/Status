package com.james.status.data.preference;

import android.content.Context;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

public class BooleanPreferenceData extends PreferenceData<Boolean> {

    public boolean value;

    public BooleanPreferenceData(Context context, Identifier identifier, boolean defaultValue, OnPreferenceChangeListener<Boolean> listener) {
        super(context, identifier, listener);

        Boolean value = PreferenceUtils.getBooleanPreference(getContext(), identifier.getPreference());
        if (value == null) value = defaultValue;
        this.value = value;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        SwitchCompat titleView = (SwitchCompat) holder.v.findViewById(R.id.title);

        titleView.setOnCheckedChangeListener(null);
        titleView.setChecked(value);
        titleView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                value = b;

                PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                if (identifier != null)
                    PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), b);
                onPreferenceChange(b);
            }
        });
    }
}
