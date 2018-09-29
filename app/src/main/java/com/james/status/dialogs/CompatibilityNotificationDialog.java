package com.james.status.dialogs;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

import androidx.appcompat.app.AppCompatDialog;
import androidx.appcompat.widget.SwitchCompat;

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

        enabledSwitchView = findViewById(R.id.enabledSwitch);

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
                PreferenceData.STATUS_NOTIFICATIONS_COMPAT.setValue(getContext(), isEnabled);
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
