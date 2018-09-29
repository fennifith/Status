package com.james.status.activities;

import android.content.DialogInterface;
import android.os.Bundle;

import com.james.status.data.AppPreferenceData;
import com.james.status.dialogs.preference.AppPreferenceDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AppSettingActivity extends AppCompatActivity {

    public final static String EXTRA_COMPONENT = "com.james.status.EXTRA_COMPONENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferenceDialog dialog = new AppPreferenceDialog(this, new AppPreferenceData(this, getIntent().getStringExtra(EXTRA_COMPONENT)));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }
}
