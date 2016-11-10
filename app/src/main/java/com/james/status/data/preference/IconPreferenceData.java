package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.dialogs.IconPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.views.IconStyleImageView;

import java.util.List;

public class IconPreferenceData extends PreferenceData<IconStyleData> {

    private IconStyleData iconStyle;
    private List<IconStyleData> iconStyles;

    public IconPreferenceData(Context context, Identifier identifier, IconStyleData iconStyle, List<IconStyleData> iconStyles, OnPreferenceChangeListener<IconStyleData> listener) {
        super(context, identifier, listener);

        if (iconStyle == null) iconStyle = iconStyles.get(0);
        this.iconStyle = iconStyle;
        this.iconStyles = iconStyles;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_icon, parent, false));
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
                    public void onPreference(PreferenceDialog dialog, IconStyleData preference) {
                        if (preference != null) {
                            ((IconStyleImageView) holder.v.findViewById(R.id.icon)).setIconStyle(preference);

                            IconPreferenceData.this.iconStyle = preference;
                            onPreferenceChange(preference);
                        }
                    }

                    @Override
                    public void onCancel(PreferenceDialog dialog) {
                    }
                });
                dialog.setTitle(getIdentifier().getTitle());
                dialog.show();
            }
        });
    }
}
