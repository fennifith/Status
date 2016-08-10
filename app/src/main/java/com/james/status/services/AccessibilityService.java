package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private PackageManager packageManager;
    private ArrayList<Notification> notifications;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        packageManager = getPackageManager();
        notifications = new ArrayList<>();

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_NOTIFICATION_STATE_CHANGED | AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16) config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

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
                            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_ADDED);
                            intent.setClass(this, StatusService.class);

                            intent.putExtra(StatusService.EXTRA_NOTIFICATION, (Notification) parcelable);
                            intent.putExtra(StatusService.EXTRA_PACKAGE_NAME, event.getPackageName());

                            startService(intent);

                            notifications.add((Notification) parcelable);
                        }
                    }
                    return;
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    final CharSequence packageName = event.getPackageName();
                    if (packageName != null && packageName.length() > 0) {
                        if (packageName.toString().equals("com.android.systemui") && event.getClassName().toString().equals("android.widget.FrameLayout")) {
                            setStatusBar(Color.BLACK, null, true);

                            if (Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2) {
                                for (Notification notification : notifications) {
                                    Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_REMOVED);
                                    intent.setClass(this, StatusService.class);

                                    intent.putExtra(StatusService.EXTRA_NOTIFICATION, notification);

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
                                            setStatusBar(color, null, false);
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
    }

    @Override
    public void onInterrupt() {}
}
