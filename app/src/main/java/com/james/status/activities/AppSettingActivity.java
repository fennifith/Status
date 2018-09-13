package com.james.status.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.james.status.R;
import com.james.status.data.AppData;

public class AppSettingActivity extends AppCompatActivity {

    public final static String EXTRA_APP = "com.james.status.EXTRA_APP";
    public final static String EXTRA_ACTIVITY = "com.james.status.EXTRA_ACTIVITY";

    private AppData app;
    private AppData.ActivityData activity;
    private ImageView colorView;

    private RecyclerView.Adapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_app_settings);

        Intent intent = getIntent();
        app = intent.getParcelableExtra(EXTRA_APP);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        final RecyclerView recycler = (RecyclerView) findViewById(R.id.recycler);
        colorView = (ImageView) findViewById(R.id.colorView);
        SwitchCompat fullscreenSwitch = (SwitchCompat) findViewById(R.id.fullscreenSwitch);
        SwitchCompat notificationSwitch = (SwitchCompat) findViewById(R.id.notificationSwitch);

        toolbar.setTitle(app.label);

        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        Boolean isFullscreen = app.getBooleanPreference(this, AppData.PreferenceIdentifier.FULLSCREEN);
        fullscreenSwitch.setChecked(isFullscreen != null && isFullscreen);

        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                app.putPreference(AppSettingActivity.this, AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
                adapter.notifyDataSetChanged();
            }
        });

        Boolean isNotifications = app.getSpecificBooleanPreference(this, AppData.PreferenceIdentifier.NOTIFICATIONS);
        notificationSwitch.setChecked(isNotifications == null || isNotifications);

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                app.putSpecificPreference(AppSettingActivity.this, AppData.PreferenceIdentifier.NOTIFICATIONS, isChecked);
            }
        });

        recycler.setLayoutManager(new GridLayoutManager(this, 1));
        recycler.setNestedScrollingEnabled(false);

        //set adapter

        if (intent.hasExtra(EXTRA_ACTIVITY)) {
            activity = intent.getParcelableExtra(EXTRA_ACTIVITY);

            //TODO: set color
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                finish();
                break;
        }

        return super.onOptionsItemSelected(item);
    }
}
