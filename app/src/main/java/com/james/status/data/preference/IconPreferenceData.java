package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.dialogs.IconPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.List;

public class IconPreferenceData extends PreferenceData {

    private IconStyleData iconStyle;
    private List<IconStyleData> iconStyles;

    public IconPreferenceData(Context context, Identifier identifier, List<IconStyleData> iconStyles, OnPreferenceChangeListener listener) {
        super(context, identifier, listener);

        iconStyle = PreferenceUtils.getObjectPreference(context, identifier.getPreference(), IconStyleData.class);
        if (iconStyle == null) iconStyle = iconStyles.get(0);

        this.iconStyles = iconStyles;
    }

    public static ViewHolder getViewHolder(Context context) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_icon, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        CustomImageView icon = (CustomImageView) holder.v.findViewById(R.id.icon);

        title.setText(getIdentifier().getTitle());

        Integer iconResource = getIconResource();
        if (iconResource != null)
            icon.setImageDrawable(ContextCompat.getDrawable(getContext(), iconResource));
        else icon.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new IconPickerDialog(getContext(), iconStyles).setPreference(iconStyle).setListener(new PreferenceDialog.OnPreferenceListener<IconStyleData>() {
                    @Override
                    public void onPreference(IconStyleData preference) {
                        CustomImageView icon = (CustomImageView) holder.v.findViewById(R.id.icon);

                        Integer iconResource = getIconResource();
                        if (iconResource != null)
                            icon.transition(ContextCompat.getDrawable(getContext(), iconResource));
                        else icon.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

                        IconPreferenceData.this.iconStyle = iconStyle;
                        PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), iconStyle);
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

    public Integer getIconResource() {
        if (getResourceSize() > 0) return iconStyle.resource[0];
        else return null;
    }

    public Integer getIconResource(int level) {
        if (level < getResourceSize()) return iconStyle.resource[level];
        else return null;
    }

    public int getResourceSize() {
        return iconStyle.resource.length;
    }
}
