package com.james.status.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
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
import android.view.ViewGroup;
import android.widget.CompoundButton;

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
    private SearchView searchView;

    private AppBarLayout appbar;
    private TabLayout tabLayout;
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

        appbar = (AppBarLayout) findViewById(R.id.appbar);
        tabLayout = (TabLayout) findViewById(R.id.tabLayout);
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

        if (StaticUtils.isAccessibilityGranted(this) && StaticUtils.isNotificationGranted(this) && StaticUtils.isPermissionsGranted(this) && !StaticUtils.isStatusServiceRunning(this) && StaticUtils.shouldShowTutorial(this, "enable")) {
            appbar.setExpanded(true, false);

            new TapTargetView.Builder(this)
                    .title(R.string.tutorial_enable)
                    .description(R.string.tutorial_enable_desc)
                    .targetCircleColor(R.color.colorAccent)
                    .textColor(android.R.color.black)
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
        } else if (searchView != null && StaticUtils.shouldShowTutorial(MainActivity.this, "search", 1)) {
            appbar.setExpanded(true, false);

            new TapTargetView.Builder(MainActivity.this)
                    .title(R.string.tutorial_search)
                    .description(R.string.tutorial_search_desc)
                    .targetCircleColor(R.color.colorAccent)
                    .textColor(android.R.color.black)
                    .drawShadow(false)
                    .listener(new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            view.dismiss(true);
                        }

                        @Override
                        public void onTargetLongClick(TapTargetView view) {
                        }
                    })
                    .cancelable(true)
                    .showFor(searchView);
        } else if (tabLayout != null && viewPager != null && viewPager.getCurrentItem() != 3 && StaticUtils.shouldShowTutorial(MainActivity.this, "faqs", 2)) {
            new TapTargetView.Builder(MainActivity.this)
                    .title(R.string.tutorial_faq)
                    .description(R.string.tutorial_faq_desc)
                    .targetCircleColor(R.color.colorAccent)
                    .textColor(android.R.color.black)
                    .drawShadow(false)
                    .listener(new TapTargetView.Listener() {
                        @Override
                        public void onTargetClick(TapTargetView view) {
                            view.dismiss(true);
                            viewPager.setCurrentItem(3, true);
                        }

                        @Override
                        public void onTargetLongClick(TapTargetView view) {
                        }
                    })
                    .cancelable(true)
                    .showFor(((ViewGroup) tabLayout.getChildAt(0)).getChildAt(3));
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(viewPager.getCurrentItem(), query);
                appbar.setExpanded(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(viewPager.getCurrentItem(), newText);
                appbar.setExpanded(true);
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
                SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
                SharedPreferences.Editor editor = prefs.edit();
                for (String key : prefs.getAll().keySet()) {
                    if (key.startsWith("tutorial")) editor.remove(key);
                }

                editor.apply();
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
