package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.james.status.dialogs.FormatDialog;
import com.james.status.dialogs.PreferenceDialog;

public class FormatPreferenceData extends BasePreferenceData<String> {

    private String value;

    public FormatPreferenceData(Context context, Identifier<String> identifier, OnPreferenceChangeListener<String> listener) {
        super(context, identifier, listener);
        value = identifier.getPreferenceValue(context);
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = new FormatDialog(getContext()).setPreference(value).setListener(new PreferenceDialog.OnPreferenceListener<String>() {
            @Override
            public void onPreference(PreferenceDialog dialog, String format) {
                value = format;

                getIdentifier().setPreferenceValue(getContext(), format);
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
