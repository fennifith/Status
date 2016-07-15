package com.james.status.activities;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.support.annotation.ColorInt;
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
import com.james.status.data.AppsColorPreferenceData;
import com.james.status.data.BooleanPreferenceData;
import com.james.status.data.ColorPreferenceData;
import com.james.status.data.ItemData;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;

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

        ArrayList<ItemData> datas = new ArrayList<>();

        datas.add(new BooleanPreferenceData(this, new ItemData.Identifier(PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO, getString(R.string.preference_bar_color_auto), getString(R.string.preference_bar_color_auto_desc), ItemData.SectionIdentifier.STATUS_BAR_COLORS), true, new BooleanPreferenceData.OnChangeListener() {
            @Override
            public void onPreferenceChange(boolean preference) {
                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        }));

        datas.add(new ColorPreferenceData(this, new ItemData.Identifier(PreferenceUtils.PreferenceIdentifier.STATUS_COLOR, getString(R.string.preference_bar_color_chooser), null, ItemData.SectionIdentifier.STATUS_BAR_COLORS), Color.BLACK, new ColorPreferenceData.OnChangeListener() {
            @Override
            public void onPreferenceChange(@ColorInt int preference) {
                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        }));

        datas.add(new AppsColorPreferenceData(this));

        datas.add(new BooleanPreferenceData(this, new ItemData.Identifier(PreferenceUtils.PreferenceIdentifier.STATUS_CLOCK_AMPM, getString(R.string.preference_ampm), getString(R.string.preference_ampm_desc), ItemData.SectionIdentifier.STATUS_BAR_ICONS), true, new BooleanPreferenceData.OnChangeListener() {
            @Override
            public void onPreferenceChange(boolean preference) {
                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        }));

        datas.add(new BooleanPreferenceData(this, new ItemData.Identifier(PreferenceUtils.PreferenceIdentifier.STATUS_BATTERY_PERCENT, getString(R.string.preference_battery_percent), getString(R.string.preference_battery_percent_desc), ItemData.SectionIdentifier.STATUS_BAR_ICONS), false, new BooleanPreferenceData.OnChangeListener() {
            @Override
            public void onPreferenceChange(boolean preference) {
                if (StaticUtils.isStatusServiceRunning(MainActivity.this)) {
                    Intent intent = new Intent(StatusService.ACTION_START);
                    intent.setClass(MainActivity.this, StatusService.class);
                    startService(intent);
                }
            }
        }));

        datas.add(new BooleanPreferenceData(this, new ItemData.Identifier(PreferenceUtils.PreferenceIdentifier.STATUS_DARK_ICONS, getString(R.string.preference_dark_icons), getString(R.string.preference_dark_icons_desc), ItemData.SectionIdentifier.STATUS_BAR_ICONS), true, null));

        adapter = new PreferenceSectionAdapter(this, datas);

        recycler.setLayoutManager(new GridLayoutManager(this, 1));
        recycler.setAdapter(adapter);
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
