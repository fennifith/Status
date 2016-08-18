package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.ActivityManager;
import android.app.Notification;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.util.ArrayMap;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public static final String ACTION_GET_COLOR = "com.james.status.ACTION_GET_COLOR";

    private PackageManager packageManager;
    private ArrayMap<String, Notification> notifications;

    private UsageStatsManager usageStatsManager;
    private ActivityManager activityManager;

    private int color = Color.BLACK;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null && action.matches(ACTION_GET_COLOR)) {
                Intent i = new Intent(StatusService.ACTION_UPDATE);
                i.setClass(this, StatusService.class);
                i.putExtra(StatusService.EXTRA_COLOR, color);
                startService(i);
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        packageManager = getPackageManager();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            usageStatsManager = (UsageStatsManager) getSystemService(Context.USAGE_STATS_SERVICE);
        activityManager = (ActivityManager) this.getSystemService(ACTIVITY_SERVICE);

        notifications = new ArrayMap<>();
        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN)
            config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            switch (event.getEventType()) {
                case AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED:
                    if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                        Parcelable parcelable = event.getParcelableData();
                        if (parcelable instanceof Notification) {
                            String key = event.getPackageName().toString() + event.getClassName().toString();

                            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_ADDED);
                            intent.setClass(this, StatusService.class);

                            intent.putExtra(StatusService.EXTRA_NOTIFICATION_KEY, key);
                            intent.putExtra(StatusService.EXTRA_NOTIFICATION, parcelable);
                            intent.putExtra(StatusService.EXTRA_PACKAGE_NAME, event.getPackageName());

                            startService(intent);

                            notifications.put(key, (Notification) parcelable);
                        }
                    }
                    return;
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    final CharSequence packageName = event.getPackageName();
                    if (packageName != null && packageName.length() > 0) {
                        if (packageName.toString().equals("com.android.systemui") && event.getClassName().toString().equals("android.widget.FrameLayout")) {
                            setStatusBar(Color.BLACK, null, true);

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                for (String key : notifications.keySet()) {
                                    Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_REMOVED);
                                    intent.setClass(this, StatusService.class);

                                    intent.putExtra(StatusService.EXTRA_NOTIFICATION_KEY, key);
                                    intent.putExtra(StatusService.EXTRA_NOTIFICATION, notifications.get(key));

                                    startService(intent);
                                }

                                notifications.clear();
                            }
                        } else {
                            new Thread() {
                                @Override
                                public void run() {
                                    final int color = ColorUtils.getStatusBarColor(AccessibilityService.this, packageManager, packageName.toString());

                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            setStatusBar(color, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                                        }
                                    });
                                }
                            }.start();
                        }
                    }
                    return;
            }
        }
    }

    private String getCurrentActivity() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            long time = System.currentTimeMillis();

            List<UsageStats> apps = usageStatsManager.queryUsageStats(UsageStatsManager.INTERVAL_DAILY, (time - 1000) * 1000, time);

            if (apps != null && apps.size() > 0) {
                SortedMap<Long, UsageStats> sortedMap = new TreeMap<>();
                for (UsageStats usageStats : apps) {
                    sortedMap.put(usageStats.getLastTimeUsed(), usageStats);
                }

                if (!sortedMap.isEmpty())
                    return sortedMap.get(sortedMap.lastKey()).getPackageName();
                else return null;
            } else return null;
        } else {
            return activityManager.getRunningTasks(1).get(0).topActivity.getPackageName();
        }
    }

    private void setStatusBar(@ColorInt int color, @Nullable Boolean fullscreen, @Nullable Boolean systemFullscreen) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto == null || isStatusColorAuto)
            intent.putExtra(StatusService.EXTRA_COLOR, color);

        if (fullscreen != null) intent.putExtra(StatusService.EXTRA_FULLSCREEN, fullscreen);
        if (systemFullscreen != null)
            intent.putExtra(StatusService.EXTRA_SYSTEM_FULLSCREEN, systemFullscreen);

        startService(intent);

        this.color = color;
    }

    @Override
    public void onInterrupt() {}
}
