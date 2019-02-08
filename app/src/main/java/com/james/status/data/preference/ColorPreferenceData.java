package com.james.status.data.preference;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.utils.StaticUtils;
import com.james.status.views.ColorView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;
import me.jfenn.colorpickerdialog.interfaces.OnColorPickedListener;
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
                .withListener(new OnColorPickedListener<ColorPickerDialog>() {
                    @Override
                    public void onColorPicked(@Nullable ColorPickerDialog pickerView, int color) {
                        value = color;
                        getIdentifier().setPreferenceValue(getContext(), color);
                        onPreferenceChange(color);

                        onBindViewHolder(new ViewHolder(v), -1);
                    }
                });

        AppCompatActivity activity = StaticUtils.getActivity(getContext());
        if (activity != null)
            dialog.show(activity.getSupportFragmentManager(), "colorPickerDialog");
    }
}
