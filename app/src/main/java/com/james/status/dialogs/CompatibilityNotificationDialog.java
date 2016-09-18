package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.support.v7.app.AppCompatDialog;
import android.support.v7.widget.SwitchCompat;
import android.view.View;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class CompatibilityNotificationDialog extends AppCompatDialog {

    private SwitchCompat enabledSwitchView;
    private boolean isEnabled;

    public CompatibilityNotificationDialog(Context context) {
        super(context, R.style.AppTheme_Dialog_FullScreen);
        isEnabled = StaticUtils.shouldUseCompatNotifications(getContext());
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.dialog_notification_compatibility);

        enabledSwitchView = (SwitchCompat) findViewById(R.id.enabledSwitch);

        enabledSwitchView.setChecked(isEnabled);
        enabledSwitchView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                isEnabled = isChecked;
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (enabledSwitchView != null) enabledSwitchView.setEnabled(true);
            }
        }, 5000);

        findViewById(R.id.confirm).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                PreferenceUtils.putPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_COMPAT, isEnabled);
                dismiss();
            }
        });

        findViewById(R.id.cancel).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                dismiss();
            }
        });
    }
}
