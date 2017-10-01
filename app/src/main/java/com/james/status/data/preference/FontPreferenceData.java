package com.james.status.data.preference;

import android.content.Context;
import android.content.DialogInterface;
import android.graphics.Typeface;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.AppCompatRadioButton;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.ScrollView;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class FontPreferenceData extends PreferenceData<String> {

    private String preference;
    private List<String> items;

    private String selectedPreference;

    public FontPreferenceData(Context context, Identifier identifier, OnPreferenceChangeListener<String> listener, String defaultItem, String... items) {
        super(context, identifier, listener);

        this.items = new ArrayList<>();
        this.items.addAll(Arrays.asList(items));
        preference = defaultItem;
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onClick(View v) {
        ScrollView scrollView = new ScrollView(getContext());

        RadioGroup group = new RadioGroup(getContext());
        int vPadding = (int) StaticUtils.getPixelsFromDp(12);
        group.setPadding(0, vPadding, 0, vPadding);

        AppCompatRadioButton normalButton = (AppCompatRadioButton) LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_radio_button, group, false);
        normalButton.setId(0);
        normalButton.setText(R.string.font_default);
        normalButton.setChecked(preference == null);
        group.addView(normalButton);

        for (int i = 0; i < items.size(); i++) {
            String item = items.get(i);

            AppCompatRadioButton button = (AppCompatRadioButton) LayoutInflater.from(getContext()).inflate(R.layout.item_dialog_radio_button, group, false);
            button.setId(i + 1);
            button.setText(item.replace(".ttf", ""));
            button.setTag(item);
            try {
                button.setTypeface(Typeface.createFromAsset(getContext().getAssets(), item));
            } catch (Exception e) {
                continue;
            }
            button.setChecked(preference != null && preference.equals(item));
            group.addView(button);
        }

        group.setOnCheckedChangeListener(new RadioGroup.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(RadioGroup group, int checkedId) {
                for (int i = 0; i < group.getChildCount(); i++) {
                    RadioButton child = (RadioButton) group.getChildAt(i);
                    child.setChecked(child.getId() == checkedId);
                    if (child.getId() == checkedId)
                        selectedPreference = (String) child.getTag();
                }
            }
        });

        scrollView.addView(group);

        new AlertDialog.Builder(getContext())
                .setTitle(getIdentifier().getTitle())
                .setView(scrollView)
                .setPositiveButton(android.R.string.ok, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        FontPreferenceData.this.preference = selectedPreference;

                        PreferenceUtils.PreferenceIdentifier identifier = getIdentifier().getPreference();
                        if (identifier != null)
                            PreferenceUtils.putPreference(getContext(), identifier, selectedPreference);

                        onPreferenceChange(selectedPreference);
                        selectedPreference = null;
                    }
                })
                .setNegativeButton(android.R.string.cancel, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        selectedPreference = null;
                    }
                })
                .show();
    }
}
