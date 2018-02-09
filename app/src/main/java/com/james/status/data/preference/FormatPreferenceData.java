package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.james.status.data.PreferenceData;
import com.james.status.dialogs.FormatDialog;
import com.james.status.dialogs.PreferenceDialog;

public class FormatPreferenceData extends BasePreferenceData<String> {

    private String value;

    public FormatPreferenceData(Context context, Identifier identifier, String defaultValue, OnPreferenceChangeListener<String> listener) {
        super(context, identifier, listener);

        com.james.status.data.PreferenceData preference = identifier.getPreference();
        this.value = preference != null ? preference.getStringValue(context, defaultValue) : defaultValue;
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = new FormatDialog(getContext()).setPreference(value).setListener(new PreferenceDialog.OnPreferenceListener<String>() {
            @Override
            public void onPreference(PreferenceDialog dialog, String format) {
                value = format;

                PreferenceData preference = getIdentifier().getPreference();
                if (preference != null)
                    preference.setValue(getContext(), format);

                onPreferenceChange(format);
            }

            @Override
            public void onCancel(PreferenceDialog dialog) {
            }
        });

        dialog.setTitle(getIdentifier().getTitle());
        dialog.show();
    }
}
