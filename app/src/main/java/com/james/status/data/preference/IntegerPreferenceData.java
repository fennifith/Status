package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.IntegerPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;

public class IntegerPreferenceData extends PreferenceData {

    int preference;
    String unit;

    public IntegerPreferenceData(Context context, Identifier identifier, int defaultValue, String unit, OnPreferenceChangeListener listener) {
        super(context, identifier, listener);

        Integer preference = PreferenceUtils.getIntegerPreference(context, identifier.getPreference());
        if (preference == null) preference = defaultValue;

        this.preference = preference;
        this.unit = unit;
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
                Dialog dialog = new IntegerPickerDialog(getContext(), unit).setPreference(preference).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                    @Override
                    public void onPreference(PreferenceDialog dialog, Integer preference) {
                        IntegerPreferenceData.this.preference = preference;
                        PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), preference);
                        onPreferenceChange();
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
}
