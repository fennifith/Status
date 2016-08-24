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
import android.support.v4.util.ArrayMap;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public static final String ACTION_GET_COLOR = "com.james.status.ACTION_GET_COLOR";

    private PackageManager packageManager;
    private ArrayMap<String, Notification> notifications;

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
                    if (StaticUtils.shouldUseCompatNotifications(this)) {
                        Parcelable parcelable = event.getParcelableData();
                        if (parcelable instanceof Notification) {
                            String key = event.getPackageName().toString() + event.getClassName().toString();

                            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT)
                                ((Notification) parcelable).extras = null;

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

                            if (StaticUtils.shouldUseCompatNotifications(this)) {
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
