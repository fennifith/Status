package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.views.ColorImageView;

public class ColorPreferenceData extends BasePreferenceData<Integer> {

    private int defaultValue, value;

    public ColorPreferenceData(Context context, Identifier identifier, @ColorInt int defaultValue, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);

        this.defaultValue = defaultValue;

        com.james.status.data.PreferenceData preference = identifier.getPreference();
        value = preference != null ? preference.getIntValue(context, defaultValue) : defaultValue;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_color, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((ColorImageView) holder.v.findViewById(R.id.color)).setColor(value);
    }

    @Override
    public void onClick(final View v) {
        Dialog dialog = new ColorPickerDialog(getContext()).setPreference(value).setDefaultPreference(defaultValue).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
            @Override
            public void onPreference(PreferenceDialog dialog, Integer color) {
                value = color;
                ((ColorImageView) v.findViewById(R.id.color)).setColor(color);

                PreferenceData preference = getIdentifier().getPreference();
                if (preference != null)
                    preference.setValue(getContext(), color);

                onPreferenceChange(color);
            }

            @Override
            public void onCancel(PreferenceDialog dialog) {
            }
        });

        dialog.setTitle(getIdentifier().getTitle());

        dialog.show();
    }
}
