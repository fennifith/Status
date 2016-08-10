package com.james.status.activities;

import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.view.View;

import com.james.status.R;
import com.james.status.services.StatusService;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class StartActivity extends AppCompatActivity {

    private static final int REQUEST_ACCESSIBILITY = 7369, REQUEST_NOTIFICATION = 2285;

    private AppCompatButton accessibilityButton, notificationButton, permissionsButton;
    private FloatingActionButton fab;

    private SharedPreferences prefs;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_start);

        prefs = PreferenceManager.getDefaultSharedPreferences(this);

        fab = (FloatingActionButton) findViewById(R.id.fab);
        accessibilityButton = (AppCompatButton) findViewById(R.id.accessibilityButton);
        notificationButton = (AppCompatButton) findViewById(R.id.notificationButton);
        permissionsButton = (AppCompatButton) findViewById(R.id.permissionsButton);

        accessibilityButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS), REQUEST_ACCESSIBILITY);
            }
        });

        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
            findViewById(R.id.notifications).setVisibility(View.GONE);
            new AlertDialog.Builder(this)
                    .setTitle(R.string.notifications_compat)
                    .setMessage(R.string.notifications_compat_desc)
                    .setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
                        @Override
                        public void onClick(DialogInterface dialogInterface, int i) {
                            dialogInterface.dismiss();
                        }
                    })
                    .create()
                    .show();
        }
        notificationButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivityForResult(new Intent("android.settings.ACTION_NOTIFICATION_LISTENER_SETTINGS"), REQUEST_NOTIFICATION);
            }
        });

        findViewById(R.id.permissions).setVisibility(StaticUtils.isPermissionsGranted(this) ? View.GONE : View.VISIBLE);
        permissionsButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                StaticUtils.isPermissionsGranted(StartActivity.this, true);
            }
        });

        fab.hide();
        fab.setImageDrawable(ImageUtils.getVectorDrawable(this, R.drawable.ic_check));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                PreferenceUtils.putPreference(StartActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED, true);

                Intent intent = new Intent(StatusService.ACTION_START);
                intent.setClass(StartActivity.this, StatusService.class);
                startService(intent);

                finish();
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        findViewById(R.id.permissions).setVisibility(StaticUtils.isPermissionsGranted(this) ? View.GONE : View.VISIBLE);

        if (StaticUtils.isAccessibilityGranted(this) && StaticUtils.isNotificationGranted(this) && StaticUtils.isPermissionsGranted(this)) fab.show();
        else fab.hide();

        prefs.edit().putBoolean("isResumed", true).apply();
    }

    @Override
    public void onBackPressed() {
        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this))
            System.exit(0);
        else super.onBackPressed();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        boolean isPermissionsGranted = StaticUtils.isPermissionsGranted(this);
        findViewById(R.id.permissions).setVisibility(isPermissionsGranted ? View.GONE : View.VISIBLE);
        if (StaticUtils.isAccessibilityGranted(this) && StaticUtils.isNotificationGranted(this) && isPermissionsGranted)
            fab.show();
        else fab.hide();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        switch (requestCode) {
            case REQUEST_ACCESSIBILITY:
                break;
            case REQUEST_NOTIFICATION:
                break;
        }
    }
}
