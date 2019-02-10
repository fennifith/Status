/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.View;

import com.james.status.dialogs.PreferenceDialog;
import com.james.status.dialogs.picker.FormatPickerDialog;

public class FormatPreferenceData extends BasePreferenceData<String> {

    private String value;

    public FormatPreferenceData(Context context, Identifier<String> identifier, OnPreferenceChangeListener<String> listener) {
        super(context, identifier, listener);
        value = identifier.getPreferenceValue(context);
    }

    @Override
    public void onClick(View v) {
        Dialog dialog = new FormatPickerDialog(getContext()).setPreference(value).setListener(new PreferenceDialog.OnPreferenceListener<String>() {
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
