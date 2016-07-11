package com.james.status.activities;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.adapters.AppColorPreviewAdapter;
import com.james.status.dialogs.AppColorDialog;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;

public class MainActivity extends AppCompatActivity {

    AppBarLayout appbar;
    SwitchCompat service;

    SwitchCompat amPm, batteryPercent, autoBarColor, darkIcons;
    View barColor, viewAppColors, addAppColors;
    CustomImageView barColorView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this))
            startActivity(new Intent(this, StartActivity.class));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        service = (SwitchCompat) findViewById(R.id.serviceEnabled);

        amPm = (SwitchCompat) findViewById(R.id.amPmEnabled);
        batteryPercent = (SwitchCompat) findViewById(R.id.batteryPercent);
        autoBarColor = (SwitchCompat) findViewById(R.id.autoBarColorEnabled);
        barColor = findViewById(R.id.pickBarColor);
        barColorView = (CustomImageView) findViewById(R.id.pickBarColor_color);
        viewAppColors = findViewById(R.id.appColors);
        addAppColors = findViewById(R.id.addAppColors);
        darkIcons = (SwitchCompat) findViewById(R.id.darkIconsEnabled);

        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        service.setChecked((enabled != null && enabled) || StaticUtils.isStatusServiceRunning(this));
        service.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                if (b) {
                    PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED, true);

                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                } else {
                    PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED, false);

                    Intent intent = new Intent(StatusService.ACTION_STOP);
                    intent.setClass(MainActivity.this, StatusService.class);
                    stopService(intent);
                }
            }
        });

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        autoBarColor.setChecked(isStatusColorAuto == null || isStatusColorAuto);
        autoBarColor.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO, b);
                barColor.setVisibility(b ? View.GONE : View.VISIBLE);

                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        });

        barColor.setVisibility(isStatusColorAuto == null || isStatusColorAuto ? View.GONE : View.VISIBLE);

        Integer statusBarColor = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (statusBarColor == null) statusBarColor = Color.BLACK;
        barColorView.setImageDrawable(new ColorDrawable(statusBarColor));

        barColor.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Integer statusBarColor = PreferenceUtils.getIntegerPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
                new ColorPickerDialog(MainActivity.this).setColor(statusBarColor != null ? statusBarColor : Color.BLACK).setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color) {
                        PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR, color);
                        barColorView.transition(new ColorDrawable(color));

                        if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                            Intent intent = new Intent(StatusService.ACTION_START);
                            intent.setClass(MainActivity.this, StatusService.class);
                            startService(intent);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
            }
        });

        viewAppColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppColorDialog(MainActivity.this).show();
            }
        });

        RecyclerView recyclerView = (RecyclerView) findViewById(R.id.appColorsRecycler);
        recyclerView.setNestedScrollingEnabled(false);
        recyclerView.setLayoutManager(new GridLayoutManager(this, 1));
        recyclerView.setAdapter(new AppColorPreviewAdapter(this).setOnSizeChangedListener(new AppColorPreviewAdapter.OnSizeChangedListener() {
            @Override
            public void onSizeChanged(int size) {
                if (size > 0) {
                    viewAppColors.setVisibility(View.VISIBLE);
                    addAppColors.setVisibility(View.GONE);
                } else {
                    viewAppColors.setVisibility(View.GONE);
                    addAppColors.setVisibility(View.VISIBLE);
                }
            }
        }));

        addAppColors.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new AppColorDialog(MainActivity.this).show();
            }
        });

        Boolean isAmPmEnabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM);
        amPm.setChecked(isAmPmEnabled == null || isAmPmEnabled);
        amPm.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM, b);

                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        });

        Boolean isBatteryPercent = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT);
        batteryPercent.setChecked(isBatteryPercent != null && isBatteryPercent);
        batteryPercent.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT, b);

                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        });

        Boolean isDarkModeEnabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS);
        darkIcons.setChecked(isDarkModeEnabled == null || isDarkModeEnabled);
        darkIcons.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                PreferenceUtils.putPreference(MainActivity.this, PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS, b);

                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);
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
