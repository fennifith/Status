package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.R;
import com.james.status.activities.AppSettingActivity;
import com.james.status.data.AppData;
import com.james.status.data.NotificationData;
import com.james.status.data.icon.NotificationsIconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    public static final String
            ACTION_GET_COLOR = "com.james.status.ACTION_GET_COLOR",
            ACTION_NOTIFY_COLOR = "com.james.status.ACTION_NOTIFY_COLOR",
            EXTRA_COMPONENT = "com.james.status.EXTRA_COMPONENT";

    private static final int NOTIFICATION_ID = 7146;

    private PackageManager packageManager;
    private NotificationManagerCompat notificationManager;
    private List<NotificationData> notifications;

    private AppData.ActivityData activityData;

    private int color = Color.BLACK;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent != null) {
            String action = intent.getAction();

            if (action != null) {
                switch (action) {
                    case ACTION_GET_COLOR:
                        Intent i = new Intent(StatusService.ACTION_UPDATE);
                        i.setClass(this, StatusService.class);
                        i.putExtra(StatusService.EXTRA_COLOR, color);
                        startService(i);
                        break;
                    case ACTION_NOTIFY_COLOR:
                        if (notificationManager != null)
                            notificationManager.cancel(NOTIFICATION_ID);

                        ComponentName component;

                        if (intent.hasExtra(EXTRA_COMPONENT))
                            component = intent.getParcelableExtra(EXTRA_COMPONENT);
                        else break;

                        AppData data;
                        try {
                            data = new AppData(packageManager, packageManager.getApplicationInfo(component.getPackageName(), PackageManager.GET_META_DATA), packageManager.getPackageInfo(component.getPackageName(), PackageManager.GET_ACTIVITIES));
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            e.printStackTrace();
                            break;
                        }

                        Intent appSettingIntent = new Intent(this, AppSettingActivity.class);
                        appSettingIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        appSettingIntent.putExtra(AppSettingActivity.EXTRA_APP, data);
                        startActivity(appSettingIntent);
                        break;
                }
            }
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        packageManager = getPackageManager();
        notificationManager = NotificationManagerCompat.from(this);

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

                            Intent intent = new Intent(NotificationsIconData.ACTION_NOTIFICATION_ADDED);
                            intent.putExtra(NotificationsIconData.EXTRA_NOTIFICATION, notification);
                            sendBroadcast(intent);

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

                                    Intent intent = new Intent(NotificationsIconData.ACTION_NOTIFICATION_REMOVED);
                                    intent.putExtra(NotificationsIconData.EXTRA_NOTIFICATION, notification);
                                    sendBroadcast(intent);
                                }

                                notifications.clear();
                            }

                            return;
                        }

                        try {
                            activityData = new AppData.ActivityData(packageManager, packageManager.getActivityInfo(new ComponentName(packageName.toString(), className.toString()), PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            if (activityData != null && !activityData.packageName.matches(packageName.toString()) && !activityData.packageName.contains(packageName) && !packageName.toString().contains(activityData.packageName))
                                setStatusBar(getDefaultColor(), null, null, false);
                            return;
                        }

                        Boolean isFullscreen = activityData.getBooleanPreference(this, AppData.PreferenceIdentifier.FULLSCREEN);

                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        ActivityInfo homeInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo;

                        if (packageName.toString().matches(homeInfo.packageName)) {
                            setStatusBar(null, true, isFullscreen, false);
                            notificationManager.cancel(NOTIFICATION_ID);
                            return;
                        }

                        Integer color = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.COLOR);
                        if (color != null) {
                            setStatusBar(color, null, isFullscreen, false);
                            return;
                        }

                        Boolean isColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
                        if (isColorAuto != null && !isColorAuto) {
                            setStatusBar(getDefaultColor(), null, isFullscreen, false);
                            return;
                        }

                        if (packageName.toString().equals("com.android.systemui")) {
                            //prevents the creation of some pretty nasty looking color schemes below Lollipop
                            setStatusBar(getDefaultColor(), null, false, false);
                            notificationManager.cancel(NOTIFICATION_ID);
                            return;
                        }

                        if (packageName.toString().matches("com.james.status")) {
                            //prevents recursive heads up notifications
                            setStatusBar(ContextCompat.getColor(this, R.color.colorPrimaryDark), null, isFullscreen, false);
                            return;
                        }

                        Integer cacheVersion = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.CACHE_VERSION);
                        if (cacheVersion != null && cacheVersion == activityData.version) {
                            color = activityData.getIntegerPreference(this, AppData.PreferenceIdentifier.CACHE_COLOR);
                        }

                        if (color == null) {
                            color = ColorUtils.getPrimaryColor(AccessibilityService.this, new ComponentName(packageName.toString(), className.toString()));

                            if (color != null) {
                                activityData.putPreference(this, AppData.PreferenceIdentifier.CACHE_COLOR, color);
                                activityData.putPreference(this, AppData.PreferenceIdentifier.CACHE_VERSION, activityData.version);
                            }
                        }

                        if (color == null) {
                            color = getDefaultColor();

                            Boolean notification = PreferenceUtils.getBooleanPreference(AccessibilityService.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS_NOTIFICATIONS);
                            if (notification != null && notification) {
                                Intent intent = new Intent(ACTION_NOTIFY_COLOR);
                                intent.setClass(AccessibilityService.this, AccessibilityService.class);
                                intent.putExtra(EXTRA_COMPONENT, new ComponentName(packageName.toString(), className.toString()));

                                notificationManager.notify(NOTIFICATION_ID, new NotificationCompat.Builder(AccessibilityService.this)
                                        .setSmallIcon(R.mipmap.ic_colorize)
                                        .setColor(ContextCompat.getColor(AccessibilityService.this, R.color.colorAccent))
                                        .setContentTitle(getString(R.string.notification_color))
                                        .setContentText(getString(R.string.notification_color_desc))
                                        .setContentIntent(PendingIntent.getService(AccessibilityService.this, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT))
                                        .build()
                                );
                            }
                        } else notificationManager.cancel(NOTIFICATION_ID);

                        setStatusBar(color, null, isFullscreen, false);
                    }
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
    public void onInterrupt() {
    }

    @Override
    public void onDestroy() {
        if (notificationManager != null) notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }
}
