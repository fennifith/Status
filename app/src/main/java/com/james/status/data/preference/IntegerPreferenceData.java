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
