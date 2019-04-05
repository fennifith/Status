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

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.james.status.R;

import androidx.appcompat.widget.SwitchCompat;

public class BooleanPreferenceData extends BasePreferenceData<Boolean> {

    public boolean value;

    public BooleanPreferenceData(Context context, Identifier<Boolean> identifier, OnPreferenceChangeListener<Boolean> listener) {
        super(context, identifier, listener);
        value = identifier.getPreferenceValue(context);
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_boolean, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);

        SwitchCompat titleView = holder.v.findViewById(R.id.title);

        titleView.setOnCheckedChangeListener(null);
        titleView.setChecked(value);
        titleView.setOnCheckedChangeListener((compoundButton, b) -> {
            value = b;

            getIdentifier().setPreferenceValue(getContext(), b);
            onPreferenceChange(b);
        });
    }
}
