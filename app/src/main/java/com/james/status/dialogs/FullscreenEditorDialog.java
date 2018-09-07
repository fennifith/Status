package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.data.AppData;

public class FullscreenEditorDialog extends AppCompatDialog {

    private AppData app;
    private AppData.ActivityData activity;

    public FullscreenEditorDialog(Context context, AppData app) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(R.string.preference_fullscreen);
        this.app = app;
    }

    public FullscreenEditorDialog(Context context, AppData.ActivityData activity) {
        super(context, R.style.AppTheme_Dialog);
        setTitle(R.string.preference_fullscreen);
        this.activity = activity;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_editor_fullscreen);

        SwitchCompat fullscreenSwitch = findViewById(R.id.fullscreenSwitch);
        SwitchCompat ignoreSwitch = findViewById(R.id.ignoreSwitch);

        Boolean isFullscreen = null;
        Boolean isIgnoring = null;

        if (app != null) {
            isFullscreen = app.getBooleanPreference(getContext(), AppData.PreferenceIdentifier.FULLSCREEN);
            isIgnoring = app.getBooleanPreference(getContext(), AppData.PreferenceIdentifier.IGNORE_AUTO_DETECT);
        } else if (activity != null) {
            isFullscreen = activity.getBooleanPreference(getContext(), AppData.PreferenceIdentifier.FULLSCREEN);
            isIgnoring = activity.getBooleanPreference(getContext(), AppData.PreferenceIdentifier.IGNORE_AUTO_DETECT);
        }

        fullscreenSwitch.setChecked(isFullscreen != null && isFullscreen);
        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (app != null)
                    app.putPreference(getContext(), AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
                else
                    activity.putPreference(getContext(), AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
            }
        });

        ignoreSwitch.setChecked(isIgnoring != null && isIgnoring);
        ignoreSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                if (app != null)
                    app.putPreference(getContext(), AppData.PreferenceIdentifier.IGNORE_AUTO_DETECT, isChecked);
                else
                    activity.putPreference(getContext(), AppData.PreferenceIdentifier.IGNORE_AUTO_DETECT, isChecked);
            }
        });
    }
}
