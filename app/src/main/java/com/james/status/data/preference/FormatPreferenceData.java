package com.james.status.data.preference;

import android.app.Dialog;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.dialogs.FormatDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.PreferenceUtils;

public class FormatPreferenceData extends PreferenceData<String> {

    private String value;

    public FormatPreferenceData(Context context, Identifier identifier, String defaultValue, OnPreferenceChangeListener<String> listener) {
        super(context, identifier, listener);

        String value = PreferenceUtils.getStringPreference(getContext(), identifier.getPreference());
        if (value == null) value = defaultValue;
        this.value = value;
    }

    @Override
    public ViewHolder getViewHolder(LayoutInflater inflater, ViewGroup parent) {
        return new ViewHolder(inflater.inflate(R.layout.item_preference_color, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        holder.v.findViewById(R.id.color).setVisibility(View.GONE);

        title.setText(getIdentifier().getTitle());

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Dialog dialog = new FormatDialog(getContext()).setPreference(value).setListener(new PreferenceDialog.OnPreferenceListener<String>() {
                    @Override
                    public void onPreference(PreferenceDialog dialog, String format) {
                        value = format;

                        PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                        if (identifier != null)
                            PreferenceUtils.putPreference(getContext(), getIdentifier().getPreference(), format);
                        onPreferenceChange(format);
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
