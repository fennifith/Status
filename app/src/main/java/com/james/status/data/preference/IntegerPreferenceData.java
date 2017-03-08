package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.support.annotation.Nullable;
import android.view.View;

import com.james.status.dialogs.IntegerPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;

public class IntegerPreferenceData extends PreferenceData<Integer> {

    private int preference;
    private String unit;

    @Nullable
    private Integer min, max;

    public IntegerPreferenceData(Context context, Identifier identifier, int defaultValue, String unit, @Nullable Integer min, @Nullable Integer max, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);

        Integer preference = PreferenceUtils.getIntegerPreference(context, identifier.getPreference());
        if (preference == null) preference = defaultValue;

        this.preference = preference;
        this.unit = unit;
        this.min = min;
        this.max = max;
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = new IntegerPickerDialog(getContext(), unit).setMinMax(min, max).setPreference(preference).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
            @Override
            public void onPreference(PreferenceDialog dialog, Integer preference) {
                IntegerPreferenceData.this.preference = preference;

                PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                if (identifier != null)
                    PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), preference);
                onPreferenceChange(preference);
            }

            @Override
            public void onCancel(PreferenceDialog dialog) {
            }
        });
        dialog.setTitle(getIdentifier().getTitle());
        dialog.show();
    }
}
