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
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.utils.StaticUtils;
import com.james.status.views.ColorView;

import androidx.appcompat.app.AppCompatActivity;
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.views.picker.ImagePickerView;

public class ColorPreferenceData extends BasePreferenceData<Integer> {

    private Integer value;
    private ValueGetter<Boolean> isAlpha;

    public ColorPreferenceData(Context context, Identifier<Integer> identifier, OnPreferenceChangeListener<Integer> listener) {
        super(context, identifier, listener);
    }

    public ColorPreferenceData withAlpha(ValueGetter<Boolean> isAlpha) {
        this.isAlpha = isAlpha;
        return this;
    }

    public boolean isAlpha() {
        return isAlpha != null ? isAlpha.get() : false;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_color, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
        value = getIdentifier().getPreferenceValue(getContext());
        ColorView colorView = holder.v.findViewById(R.id.color);
        if (value != null && !value.equals(getNullValue())) {
            colorView.setVisibility(View.VISIBLE);
            colorView.setColor(value);
        } else colorView.setVisibility(View.GONE);
    }

    @Override
    public void onClick(final View v) {
        ColorPickerDialog dialog = new ColorPickerDialog()
                .withTitle(getIdentifier().getTitle())
                .withAlphaEnabled(isAlpha())
                .withColor(value != null && !value.equals(getNullValue()) ? value : Color.BLACK)
                .withPresets()
                .withPicker(ImagePickerView.class)
                .withListener((pickerView, color) -> {
                    value = color;
                    getIdentifier().setPreferenceValue(getContext(), color);
                    onPreferenceChange(color);

                    onBindViewHolder(new ViewHolder(v), -1);
                });

        AppCompatActivity activity = StaticUtils.getActivity(getContext());
        if (activity != null)
            dialog.show(activity.getSupportFragmentManager(), "colorPickerDialog");
    }
}
