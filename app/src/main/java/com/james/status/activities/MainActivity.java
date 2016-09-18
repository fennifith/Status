package com.james.status.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CompoundButton;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.getkeepsafe.taptargetview.TapTargetView;
import com.james.status.R;
import com.james.status.Status;
import com.james.status.adapters.SimplePagerAdapter;
import com.james.status.fragments.AppPreferenceFragment;
import com.james.status.fragments.FaqFragment;
import com.james.status.fragments.GeneralPreferenceFragment;
import com.james.status.fragments.IconPreferenceFragment;
import com.james.status.services.StatusService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class MainActivity extends AppCompatActivity {

    private Status status;

    private SwitchCompat service;
    private AppBarLayout appbar;
    private ViewPager viewPager;
    private SimplePagerAdapter adapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        status = (Status) getApplicationContext();

        if (!StaticUtils.isAccessibilityGranted(this) || !StaticUtils.isNotificationGranted(this) || !StaticUtils.isPermissionsGranted(this))
            startActivity(new Intent(this, StartActivity.class));

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));

        Glide.with(this).load("https://theandroidmaster.github.io/images/headers/status_bg.png").into((ImageView) findViewById(R.id.header));

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        TabLayout tabLayout = (TabLayout) findViewById(R.id.tabLayout);
        service = (SwitchCompat) findViewById(R.id.serviceEnabled);
        viewPager = (ViewPager) findViewById(R.id.viewPager);

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

        adapter = new SimplePagerAdapter(this, getSupportFragmentManager(), viewPager, new GeneralPreferenceFragment(), new IconPreferenceFragment(), new AppPreferenceFragment(), new FaqFragment());
        viewPager.setAdapter(adapter);

        tabLayout.setupWithViewPager(viewPager);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (StaticUtils.isAccessibilityGranted(this) && StaticUtils.isNotificationGranted(this) && StaticUtils.isPermissionsGranted(this) && !StaticUtils.isStatusServiceRunning(this)) {
            new TapTargetView.Builder(this)
                    .title(R.string.tutorial_enable)
                    .description(R.string.tutorial_enable_desc)
                    .outerCircleColor(R.color.colorAccent)
                    .dimColor(android.R.color.black)
                    .drawShadow(false)
                    .listener(new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            service.performClick();
                            view.dismiss(true);
                        }

                        @Override
                        public void onTargetLongClick(TapTargetView view) {
                        }
                    })
                    .cancelable(true)
                    .showFor(service);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        SearchView searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(viewPager.getCurrentItem(), query);
                appbar.setExpanded(false);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(viewPager.getCurrentItem(), newText);
                appbar.setExpanded(false);
                return true;
            }
        });

        searchView.setOnCloseListener(new SearchView.OnCloseListener() {
            @Override
            public boolean onClose() {
                adapter.filter(viewPager.getCurrentItem(), null);
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
            case R.id.action_tutorial:
                //TODO: do something
                break;
            case R.id.action_about:
                startActivity(new Intent(this, AboutActivity.class));
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        status.onActivityResult(requestCode, resultCode, data);
    }
}
