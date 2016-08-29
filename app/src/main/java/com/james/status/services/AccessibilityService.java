package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.data.NotificationData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public static final String ACTION_GET_COLOR = "com.james.status.ACTION_GET_COLOR";

    private PackageManager packageManager;
    private List<NotificationData> notifications;

    private int color = Color.BLACK;
    private String packageName;

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

        notifications = new ArrayList<>();
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
                            NotificationData notification = new NotificationData((Notification) parcelable, event.getPackageName().toString());

                            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_ADDED);
                            intent.setClass(this, StatusService.class);
                            intent.putExtra(StatusService.EXTRA_NOTIFICATION, notification);

                            startService(intent);

                            notifications.add(notification);
                        }
                    }
                    return;
                case AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED:
                    final CharSequence packageName = event.getPackageName();
                    final CharSequence className = event.getClassName();
                    if (packageName != null && packageName.length() > 0 && className != null && className.length() > 0) {
                        if (packageName.toString().equals("com.android.systemui") && className.toString().equals("android.widget.FrameLayout")) {
                            setStatusBar(null, null, null, true);

                            if (StaticUtils.shouldUseCompatNotifications(this)) {
                                for (NotificationData notification : notifications) {

                                    Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_REMOVED);
                                    intent.setClass(this, StatusService.class);
                                    intent.putExtra(StatusService.EXTRA_NOTIFICATION, notification);

                                    startService(intent);
                                }

                                notifications.clear();
                            }
                        } else if (packageName.toString().equals("com.android.systemui")) {
                            setStatusBar(getDefaultColor(), null, false, false);
                        } else {
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            String homePackageName = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;

                            if (packageName.toString().contains(homePackageName) || packageName.toString().matches(homePackageName)) {
                                setStatusBar(null, true, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                                return;
                            }

                            new Thread() {
                                @Override
                                public void run() {
                                    final Integer color = ColorUtils.getStatusBarColor(AccessibilityService.this, new ComponentName(packageName.toString(), className.toString()), null);

                                    new Handler(getMainLooper()).post(new Runnable() {
                                        @Override
                                        public void run() {
                                            Integer statusBarColor;

                                            if (color == null && (AccessibilityService.this.packageName == null || !packageName.toString().matches(AccessibilityService.this.packageName))) {
                                                statusBarColor = getDefaultColor();
                                                AccessibilityService.this.packageName = packageName.toString();
                                            } else statusBarColor = color;

                                            setStatusBar(statusBarColor, null, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
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

    @ColorInt
    private int getDefaultColor() {
        Integer color = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        return color;
    }

    private void setStatusBar(@Nullable @ColorInt Integer color, @Nullable Boolean isHomeScreen, @Nullable Boolean isFullscreen, @Nullable Boolean isSystemFullscreen) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        if (color != null) intent.putExtra(StatusService.EXTRA_COLOR, color);

        if (isHomeScreen != null) intent.putExtra(StatusService.EXTRA_IS_HOME_SCREEN, isHomeScreen);

        if (isFullscreen != null) intent.putExtra(StatusService.EXTRA_IS_FULLSCREEN, isFullscreen);

        if (isSystemFullscreen != null)
            intent.putExtra(StatusService.EXTRA_IS_SYSTEM_FULLSCREEN, isSystemFullscreen);

        startService(intent);

        if (color != null) this.color = color;
    }

    @Override
    public void onInterrupt() {}
}
