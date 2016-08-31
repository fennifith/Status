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
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.ActivityColorData;
import com.james.status.data.NotificationData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
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
                        Integer defaultColor = PreferenceUtils.getIntegerPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
                        if (defaultColor == null) defaultColor = Color.BLACK;

                        ComponentName component;

                        if (intent.hasExtra(EXTRA_COMPONENT))
                            component = intent.getParcelableExtra(EXTRA_COMPONENT);
                        else break;

                        ActivityColorData data;
                        try {
                            data = new ActivityColorData(packageManager, packageManager.getActivityInfo(component, PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException e) {
                            e.printStackTrace();
                            break;
                        }

                        PreferenceDialog dialog = new ColorPickerDialog(this).setDefaultPreference(defaultColor).setTag(data).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                            @Override
                            public void onPreference(PreferenceDialog dialog, Integer preference) {
                                Gson gson = new Gson();

                                List<String> jsons = PreferenceUtils.getStringListPreference(AccessibilityService.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS);
                                if (jsons == null) jsons = new ArrayList<>();

                                ActivityColorData app = (ActivityColorData) dialog.getTag();
                                app.color = preference;

                                jsons.add(gson.toJson(app));

                                PreferenceUtils.putPreference(AccessibilityService.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS, jsons);
                                setStatusBar(preference, null, null, null);
                            }

                            @Override
                            public void onCancel(PreferenceDialog dialog) {
                                notificationManager.cancel(NOTIFICATION_ID);
                            }
                        });

                        dialog.setTitle(data.label);
                        dialog.getWindow().setType(WindowManager.LayoutParams.TYPE_SYSTEM_ALERT);
                        dialog.show();
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

                            return;
                        }

                        if (packageName.toString().equals("com.android.systemui")) {
                            setStatusBar(getDefaultColor(), null, false, false);
                            notificationManager.cancel(NOTIFICATION_ID);
                            return;
                        }

                        Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                        homeIntent.addCategory(Intent.CATEGORY_HOME);
                        ActivityInfo homeInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo;

                        if (packageName.toString().matches(homeInfo.packageName) && className.toString().matches(homeInfo.name)) {
                            setStatusBar(null, true, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                            notificationManager.cancel(NOTIFICATION_ID);
                            return;
                        }

                        List<String> apps = PreferenceUtils.getStringListPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS);
                        if (apps != null) {
                            Gson gson = new Gson();
                            for (String app : apps) {
                                ActivityColorData data = gson.fromJson(app, ActivityColorData.class);
                                if (packageName.toString().matches(data.packageName) && (data.name == null || className.toString().matches(data.name)) && data.color != null) {
                                    setStatusBar(data.color, null, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                                    notificationManager.cancel(NOTIFICATION_ID);
                                    return;
                                }
                            }
                        }

                        Boolean isColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
                        if (isColorAuto != null && !isColorAuto) {
                            setStatusBar(getDefaultColor(), null, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                            return;
                        }

                        if (packageName.toString().matches("com.james.status")) {
                            setStatusBar(ContextCompat.getColor(this, R.color.colorPrimaryDark), null, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                            return;
                        }

                        new Thread() {
                            @Override
                            public void run() {
                                final Integer color = ColorUtils.getPrimaryColor(AccessibilityService.this, new ComponentName(packageName.toString(), className.toString()));

                                new Handler(getMainLooper()).post(new Runnable() {
                                    @Override
                                    public void run() {
                                        Integer statusBarColor;

                                        if (color == null) {
                                            statusBarColor = getDefaultColor();

                                            Boolean notification = PreferenceUtils.getBooleanPreference(AccessibilityService.this, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS_NOTIFICATIONS);
                                            if (notification == null || notification) {
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
                                        } else {
                                            statusBarColor = color;
                                            notificationManager.cancel(NOTIFICATION_ID);
                                        }

                                        setStatusBar(statusBarColor, null, StaticUtils.isStatusBarFullscreen(AccessibilityService.this, packageName.toString()), false);
                                    }
                                });
                            }
                        }.start();
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
}
