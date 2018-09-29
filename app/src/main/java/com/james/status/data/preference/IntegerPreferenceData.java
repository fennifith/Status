package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.james.status.dialogs.PreferenceDialog;
import com.james.status.dialogs.picker.IntegerPickerDialog;

import androidx.annotation.Nullable;

public class IntegerPreferenceData extends BasePreferenceData<Integer> {

    private int preference;
    private String unit;

    @Nullable
    private Integer min, max;

    public IntegerPreferenceData(Context context, Identifier<Integer> identifier, String unit, @Nullable Integer min, @Nullable Integer max, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);
        preference = identifier.getPreferenceValue(context);

        this.unit = unit;
        this.min = min;
        this.max = max;
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = new IntegerPickerDialog(getContext(), unit).setMinMax(min, max).setPreference(preference).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
            @Override
            public void onPreference(PreferenceDialog dialog, Integer value) {
                preference = value;

                getIdentifier().setPreferenceValue(getContext(), value);
                onPreferenceChange(value);
            }

            @Override
            public void onCancel(PreferenceDialog dialog) {
            }
        });
        dialog.setTitle(getIdentifier().getTitle());
        dialog.show();
    }
}
