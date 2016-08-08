package com.james.status.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.james.status.R;
import com.james.status.adapters.PreferenceSectionAdapter;
import com.james.status.data.IconStyleData;
import com.james.status.data.preference.AppsColorPreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.Arrays;

public class MainActivity extends AppCompatActivity {

    AppBarLayout appbar;
    SwitchCompat service;

    RecyclerView recycler;
    PreferenceSectionAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this))
            startActivity(new Intent(this, StartActivity.class));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Glide.with(this).load("https://theandroidmaster.github.io/images/headers/status_bg.png").into((ImageView) findViewById(R.id.header));

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        service = (SwitchCompat) findViewById(R.id.serviceEnabled);
        recycler = (RecyclerView) findViewById(R.id.recycler);

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

        adapter = new PreferenceSectionAdapter(this, Arrays.asList(
                new BooleanPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO,
                                getString(R.string.preference_bar_color_auto), getString(R.string.preference_bar_color_auto_desc),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new ColorPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_COLOR,
                                getString(R.string.preference_bar_color_chooser),
                                PreferenceData.Identifier.SectionIdentifier.COLORS
                        ),
                        Color.BLACK,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new AppsColorPreferenceData(this),
                new BooleanPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM,
                                getString(R.string.preference_ampm),
                                getString(R.string.preference_ampm_desc),
                                PreferenceData.Identifier.SectionIdentifier.CLOCK
                        ),
                        true,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS,
                                getString(R.string.preference_dark_icons),
                                getString(R.string.preference_dark_icons_desc),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        true,
                        null
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.BATTERY_ICON_STYLE,
                                getString(R.string.preference_battery_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_battery_alert,
                                        R.drawable.ic_battery_20,
                                        R.drawable.ic_battery_30,
                                        R.drawable.ic_battery_50,
                                        R.drawable.ic_battery_60,
                                        R.drawable.ic_battery_80,
                                        R.drawable.ic_battery_90,
                                        R.drawable.ic_battery_full,
                                        R.drawable.ic_battery_charging_20,
                                        R.drawable.ic_battery_charging_30,
                                        R.drawable.ic_battery_charging_50,
                                        R.drawable.ic_battery_charging_60,
                                        R.drawable.ic_battery_charging_80,
                                        R.drawable.ic_battery_charging_90,
                                        R.drawable.ic_battery_charging_full
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_circle),
                                        R.drawable.ic_battery_circle_alert,
                                        R.drawable.ic_battery_circle_20,
                                        R.drawable.ic_battery_circle_30,
                                        R.drawable.ic_battery_circle_50,
                                        R.drawable.ic_battery_circle_60,
                                        R.drawable.ic_battery_circle_80,
                                        R.drawable.ic_battery_circle_90,
                                        R.drawable.ic_battery_circle_full,
                                        R.drawable.ic_battery_circle_charging_20,
                                        R.drawable.ic_battery_circle_charging_30,
                                        R.drawable.ic_battery_circle_charging_50,
                                        R.drawable.ic_battery_circle_charging_60,
                                        R.drawable.ic_battery_circle_charging_80,
                                        R.drawable.ic_battery_circle_charging_90,
                                        R.drawable.ic_battery_circle_charging_full
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_circle_outline),
                                        R.drawable.ic_battery_circle_outline_alert,
                                        R.drawable.ic_battery_circle_outline_20,
                                        R.drawable.ic_battery_circle_outline_30,
                                        R.drawable.ic_battery_circle_outline_50,
                                        R.drawable.ic_battery_circle_outline_60,
                                        R.drawable.ic_battery_circle_outline_80,
                                        R.drawable.ic_battery_circle_outline_90,
                                        R.drawable.ic_battery_circle_outline_full,
                                        R.drawable.ic_battery_circle_outline_20,
                                        R.drawable.ic_battery_circle_outline_30,
                                        R.drawable.ic_battery_circle_outline_50,
                                        R.drawable.ic_battery_circle_outline_60,
                                        R.drawable.ic_battery_circle_outline_80,
                                        R.drawable.ic_battery_circle_outline_90,
                                        R.drawable.ic_battery_circle_outline_full
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.NETWORK_ICON_STYLE,
                                getString(R.string.preference_network_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_signal_0,
                                        R.drawable.ic_signal_1,
                                        R.drawable.ic_signal_2,
                                        R.drawable.ic_signal_3,
                                        R.drawable.ic_signal_4
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.WIFI_ICON_STYLE,
                                getString(R.string.preference_wifi_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_wifi_0,
                                        R.drawable.ic_wifi_1,
                                        R.drawable.ic_wifi_2,
                                        R.drawable.ic_wifi_3,
                                        R.drawable.ic_wifi_4
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.GPS_ICON_STYLE,
                                getString(R.string.preference_gps_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_gps_searching,
                                        R.drawable.ic_gps_fixed
                                ),
                                new IconStyleData(
                                        getString(R.string.icon_style_dish),
                                        R.drawable.ic_gps_dish_searching,
                                        R.drawable.ic_gps_dish_fixed
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.BLUETOOTH_ICON_STYLE,
                                getString(R.string.preference_bluetooth_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_bluetooth,
                                        R.drawable.ic_bluetooth_connected
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.AIRPLANE_MODE_ICON_STYLE,
                                getString(R.string.preference_airplane_mode_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_airplane
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new IconPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.ALARM_ICON_STYLE,
                                getString(R.string.preference_alarm_icon),
                                PreferenceData.Identifier.SectionIdentifier.ICONS
                        ),
                        Arrays.asList(
                                new IconStyleData(
                                        getString(R.string.icon_style_default),
                                        R.drawable.ic_alarm
                                )
                        ),
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                ),
                new BooleanPreferenceData(
                        this,
                        new PreferenceData.Identifier(
                                PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT,
                                getString(R.string.preference_battery_percent),
                                getString(R.string.preference_battery_percent_desc),
                                PreferenceData.Identifier.SectionIdentifier.OTHER
                        ),
                        false,
                        new PreferenceData.OnPreferenceChangeListener() {
                            @Override
                            public void onPreferenceChange() {
                                updateService();
                            }
                        }
                )
        ));

        recycler.setLayoutManager(new GridLayoutManager(this, 1));
        recycler.setAdapter(adapter);
    }

    private void updateService() {
        if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
            Intent intent = new Intent(StatusService.ACTION_START);
            intent.setClass(MainActivity.this, StatusService.class);
            startService(intent);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(query);
                appbar.setExpanded(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(newText);
                appbar.setExpanded(false);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.filter(null);
                if (recycler.getScrollY() == 0) appbar.setExpanded(true);
                return false;
            }
        });

        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_setup:
                startActivity(new Intent(this, StartActivity.class));
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
