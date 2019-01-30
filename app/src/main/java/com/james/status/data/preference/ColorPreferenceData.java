package com.james.status.data.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.views.ColorView;

import me.jfenn.colorpickerdialog.dialogs.ColorPickerDialog;

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
        ColorPickerDialog dialog = new ColorPickerDialog(getContext()).withAlphaEnabled(isAlpha())
                .withColor(value != null && !value.equals(getNullValue()) ? value : Color.BLACK)
                .withListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(ColorPickerDialog colorPickerDialog, int i) {
                        value = i;
                        onBindViewHolder(new ViewHolder(v), -1);

                        getIdentifier().setPreferenceValue(getContext(), i);
                        onPreferenceChange(i);
                    }
                });

        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                onBindViewHolder(new ViewHolder(v), -1);
            }
        });

        dialog.setTitle(getIdentifier().getTitle());
        dialog.show();
    }
}
