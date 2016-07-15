package com.james.status.data;

import android.content.Context;
import android.graphics.drawable.ColorDrawable;
import android.support.annotation.ColorInt;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

public class ColorPreferenceData extends ItemData {

    public int value;
    private OnChangeListener listener;

    public ColorPreferenceData(Context context, Identifier identifier, @ColorInt int defaultValue, OnChangeListener listener) {
        super(context, identifier);

        Integer value = PreferenceUtils.getIntegerPreference(getContext(), identifier.getPreference());
        if (value == null) value = defaultValue;
        this.value = value;

        this.listener = listener;
    }

    public static ViewHolder getViewHolder(Context context) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_color, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        CustomImageView color = (CustomImageView) holder.v.findViewById(R.id.color);

        title.setText(getIdentifier().getTitle());
        color.setImageDrawable(new ColorDrawable(value));

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new ColorPickerDialog(getContext()).setColor(value).setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color) {
                        value = color;
                        ((CustomImageView) holder.v.findViewById(R.id.color)).transition(new ColorDrawable(color));
                        PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), color);
                        if (listener != null) listener.onPreferenceChange(color);
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
            }
        });
    }

    public interface OnChangeListener {
        void onPreferenceChange(@ColorInt int preference);
    }

}
