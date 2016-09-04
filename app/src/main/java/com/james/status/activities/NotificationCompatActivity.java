package com.james.status.activities;

import android.app.Activity;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

public class NotificationCompatActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FrameLayout(this));

        new AlertDialog.Builder(this)
                .setTitle(R.string.notifications_compat)
                .setMessage(R.string.notifications_compat_desc)
                .setPositiveButton(R.string.action_enable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int i) {
                        PreferenceUtils.putPreference(NotificationCompatActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_COMPAT, true);
                        dialog.dismiss();

                        setResult(Activity.RESULT_OK, new Intent());
                        finish();
                    }
                })
                .setNegativeButton(R.string.action_disable, new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        PreferenceUtils.putPreference(NotificationCompatActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_COMPAT, false);
                        dialog.dismiss();

                        setResult(Activity.RESULT_CANCELED, new Intent());
                        finish();
                    }
                })
                .setOnDismissListener(new DialogInterface.OnDismissListener() {
                    @Override
                    public void onDismiss(DialogInterface dialog) {
                        dialog.dismiss();
                        setResult(Activity.RESULT_CANCELED, new Intent());
                        finish();
                    }
                })
                .create()
                .show();
    }
}
