package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.views.CircleColorView;

public class ColorPreferenceData extends BasePreferenceData<Integer> {

    private int value;
    private boolean isAlpha;

    public ColorPreferenceData(Context context, Identifier<Integer> identifier, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);

        value = identifier.getPreferenceValue(context);
    }

    public ColorPreferenceData withAlpha(boolean isAlpha) {
        this.isAlpha = isAlpha;
        return this;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_color, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        ((CircleColorView) holder.v.findViewById(R.id.color)).setColor(value);
    }

    @Override
    public void onClick(final View v) {
        Dialog dialog = new ColorPickerDialog(getContext()).withAlpha(isAlpha).setPreference(value).setDefaultPreference((int) getIdentifier().getPreference().getDefaultValue()).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
            @Override
            public void onPreference(PreferenceDialog dialog, Integer color) {
                value = color;
                ((CircleColorView) v.findViewById(R.id.color)).setColor(color);

                getIdentifier().setPreferenceValue(getContext(), color);
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
