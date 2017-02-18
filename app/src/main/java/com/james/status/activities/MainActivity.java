package com.james.status.activities;

import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.annotation.StringRes;
import android.support.design.widget.AppBarLayout;
import android.support.design.widget.BottomSheetBehavior;
import android.support.design.widget.TabLayout;
import android.support.v4.view.MenuItemCompat;
import android.support.v4.view.ViewCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.SearchView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.CompoundButton;
import android.widget.TextView;

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

import james.tooltips.utils.ViewUtils;

public class MainActivity extends AppCompatActivity implements ViewPager.OnPageChangeListener {

    private Status status;

    private SwitchCompat service;
    private SearchView searchView;

    private AppBarLayout appbar;
    private TabLayout tabLayout;
    private ViewPager viewPager;
    private SimplePagerAdapter adapter;
    private View bottomSheet, expand;
    private TextView title, content;

    private BottomSheetBehavior behavior;
    private MenuItem resetItem, notificationItem;

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
        bottomSheet = findViewById(R.id.bottomSheet);
        expand = findViewById(R.id.expand);
        title = (TextView) findViewById(R.id.title);
        content = (TextView) findViewById(R.id.content);

        ViewCompat.setElevation(bottomSheet, ViewUtils.dpToPx(10));

        behavior = BottomSheetBehavior.from(bottomSheet);
        behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
        behavior.setBottomSheetCallback(new BottomSheetBehavior.BottomSheetCallback() {
            @Override
            public void onStateChanged(@NonNull View bottomSheet, int newState) {
                if (newState == BottomSheetBehavior.STATE_EXPANDED) {
                    content.animate().alpha(1).start();
                    expand.animate().alpha(0).start();
                } else {
                    content.animate().alpha(0).start();
                    expand.animate().alpha(1).start();
                }
            }

            @Override
            public void onSlide(@NonNull View bottomSheet, float slideOffset) {

            }
        });

        bottomSheet.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (behavior.getState() == BottomSheetBehavior.STATE_COLLAPSED)
                    behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
                else if (behavior.getState() == BottomSheetBehavior.STATE_EXPANDED) {
                    OnTutorialClickListener listener = (OnTutorialClickListener) v.getTag();
                    if (listener != null) listener.onClick();
                    behavior.setState(BottomSheetBehavior.STATE_HIDDEN);
                }
            }
        });

        expand.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                behavior.setState(BottomSheetBehavior.STATE_EXPANDED);
            }
        });

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
        viewPager.addOnPageChangeListener(this);

        tabLayout.setupWithViewPager(viewPager);
    }

    public void setTutorial(@StringRes int titleRes, @StringRes int contentRes, OnTutorialClickListener listener) {
        title.setText(titleRes);
        content.setText(contentRes);
        behavior.setState(BottomSheetBehavior.STATE_COLLAPSED);
        bottomSheet.setTag(listener);
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            if (StaticUtils.isAccessibilityGranted(this) && StaticUtils.isNotificationGranted(this) && StaticUtils.isPermissionsGranted(this) && !StaticUtils.isStatusServiceRunning(this) && StaticUtils.shouldShowTutorial(this, "enable")) {
                appbar.setExpanded(true, false);
                setTutorial(R.string.tutorial_enable, R.string.tutorial_enable_desc, new OnTutorialClickListener() {
                    @Override
                    public void onClick() {
                        service.setChecked(true);
                    }
                });
            } else if (searchView != null && StaticUtils.shouldShowTutorial(MainActivity.this, "search", 1)) {
                appbar.setExpanded(true, false);
                setTutorial(R.string.tutorial_search, R.string.tutorial_search_desc, null);
            } else if (tabLayout != null && viewPager != null && viewPager.getCurrentItem() != 3 && StaticUtils.shouldShowTutorial(MainActivity.this, "faqs", 2)) {
                setTutorial(R.string.tutorial_faq, R.string.tutorial_faq_desc, new OnTutorialClickListener() {
                    @Override
                    public void onClick() {
                        viewPager.setCurrentItem(3);
                    }
                });
            }
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_main, menu);

        resetItem = menu.findItem(R.id.action_reset);
        notificationItem = menu.findItem(R.id.action_toggle_notifications);
        searchView = (SearchView) MenuItemCompat.getActionView(menu.findItem(R.id.action_search));

        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                adapter.filter(viewPager.getCurrentItem(), query.toLowerCase());
                appbar.setExpanded(true);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                adapter.filter(viewPager.getCurrentItem(), newText.toLowerCase());
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
            case R.id.action_reset:
                if (adapter.getItem(viewPager.getCurrentItem()) instanceof AppPreferenceFragment)
                    ((AppPreferenceFragment) adapter.getItem(viewPager.getCurrentItem())).reset();
                break;
            case R.id.action_toggle_notifications:
                if (adapter.getItem(viewPager.getCurrentItem()) instanceof AppPreferenceFragment) {
                    boolean isNotifications = ((AppPreferenceFragment) adapter.getItem(viewPager.getCurrentItem())).isNotifications();
                    ((AppPreferenceFragment) adapter.getItem(viewPager.getCurrentItem())).setNotifications(!isNotifications);
                    item.setTitle(!isNotifications ? R.string.notifications_disable : R.string.notifications_enable);
                }
                break;
        }
        return super.onOptionsItemSelected(item);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        status.onActivityResult(requestCode, resultCode, data);
    }

    @Override
    public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {

    }

    @Override
    public void onPageSelected(int position) {
        if (resetItem != null && notificationItem != null) {
            if (adapter.getItem(position) instanceof AppPreferenceFragment) {
                resetItem.setVisible(true);
                notificationItem.setVisible(true);
                notificationItem.setTitle(((AppPreferenceFragment) adapter.getItem(position)).isNotifications() ? R.string.notifications_disable : R.string.notifications_enable);
            } else {
                resetItem.setVisible(false);
                notificationItem.setVisible(false);
            }
        }

        if (behavior != null && behavior.getState() == BottomSheetBehavior.STATE_HIDDEN) {
            switch (position) {
                case 1:
                    if (StaticUtils.shouldShowTutorial(this, "disableicon")) {
                        setTutorial(R.string.tutorial_icon_switch, R.string.tutorial_icon_switch_desc, null);
                    } else if (StaticUtils.shouldShowTutorial(this, "moveicon", 1)) {
                        setTutorial(R.string.tutorial_icon_order, R.string.tutorial_icon_order_desc, null);
                    }
                    break;
                case 2:
                    if (StaticUtils.shouldShowTutorial(this, "activities")) {
                        setTutorial(R.string.tutorial_activities, R.string.tutorial_activities_desc, null);
                    }
                    break;
            }
        }
    }

    @Override
    public void onPageScrollStateChanged(int state) {

    }

    public interface OnTutorialClickListener {
        void onClick();
    }
}
