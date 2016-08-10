package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.dialogs.IconPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.IconStyleImageView;

import java.util.List;

public class IconPreferenceData extends PreferenceData {

    private IconStyleData iconStyle;
    private List<IconStyleData> iconStyles;

    public IconPreferenceData(Context context, Identifier identifier, List<IconStyleData> iconStyles, OnPreferenceChangeListener listener) {
        super(context, identifier, listener);

        int[] resource = PreferenceUtils.getIntegerArrayPreference(context, getIdentifier().getPreference());
        if (resource != null) {
            for (IconStyleData style : iconStyles) {
                if (style.resource == resource) iconStyle = style;
            }
        } else iconStyle = iconStyles.get(0);

        this.iconStyles = iconStyles;
    }

    public static ViewHolder getViewHolder(Context context) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_icon, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        IconStyleImageView icon = (IconStyleImageView) holder.v.findViewById(R.id.icon);

        title.setText(getIdentifier().getTitle());

        icon.setIconStyle(iconStyle);

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new IconPickerDialog(getContext(), iconStyles).setPreference(iconStyle).setListener(new PreferenceDialog.OnPreferenceListener<IconStyleData>() {
                    @Override
                    public void onPreference(IconStyleData preference) {
                        ((IconStyleImageView) holder.v.findViewById(R.id.icon)).setIconStyle(preference);

                        IconPreferenceData.this.iconStyle = iconStyle;
                        PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), iconStyle.resource);
                        onPreferenceChange();
                    }

                    @Override
                    public void onCancel() {
                    }
                });
                dialog.setTitle(getIdentifier().getTitle());
                dialog.show();
            }
        });
    }
}
