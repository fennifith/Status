package com.james.status.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.AppCompatButton;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;

import com.james.status.R;
import com.james.status.services.StatusService;
import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class MainActivity extends AppCompatActivity {

    AppBarLayout appbar;
    AppCompatButton service;
    FloatingActionButton fab;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this))
            startActivity(new Intent(this, StartActivity.class));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        service = (AppCompatButton) findViewById(R.id.service);
        fab = (FloatingActionButton) findViewById(R.id.fab);

        fab.setImageDrawable(ImageUtils.getVectorDrawable(this, R.drawable.ic_expand));
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                appbar.setExpanded(false, true);
            }
        });

        service.setText(StaticUtils.isStatusServiceRunning(this) ? R.string.service_stop : R.string.service_start);
        service.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED, false);
                    service.setText(R.string.service_start);

                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                } else {
                    PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED, true);
                    service.setText(R.string.service_stop);

                    Intent intent = new Intent(StatusService.ACTION_STOP);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
        menu.findItem(R.id.action_setup).setIcon(ImageUtils.getVectorDrawable(this, R.drawable.ic_setup));
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup:
                startActivity(new Intent(this, StartActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
