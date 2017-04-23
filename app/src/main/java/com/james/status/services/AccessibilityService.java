package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Parcelable;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.NotificationCompat;
import android.view.accessibility.AccessibilityEvent;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.activities.AppSettingActivity;
import com.james.status.data.AppData;
import com.james.status.data.NotificationData;
import com.james.status.data.icon.NotificationsIconData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

import java.lang.ref.SoftReference;
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
    private VolumeReceiver volumeReceiver;

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

        volumeReceiver = new VolumeReceiver(this);
        registerReceiver(volumeReceiver, new IntentFilter("android.media.VOLUME_CHANGED_ACTION"));

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
                    if (StaticUtils.shouldUseCompatNotifications(this) && !event.getPackageName().toString().matches("com.james.status")) {
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
                    Status.showDebug(this, event.toString(), Toast.LENGTH_LONG);

                    if (packageManager != null && packageName != null && packageName.length() > 0 && className != null && className.length() > 0) {
                        try {
                            activityData = new AppData.ActivityData(packageManager, packageManager.getActivityInfo(new ComponentName(packageName.toString(), className.toString()), PackageManager.GET_META_DATA));
                        } catch (PackageManager.NameNotFoundException | NullPointerException e) {
                            if (activityData != null) {
                                if (packageName.toString().equals("com.android.systemui")) {
                                    if (event.getText().toString().toLowerCase().contains("volume")) {
                                        if (event.getText().toString().toLowerCase().contains("hidden")) {
                                            volumeReceiver.cancel();
                                            setStatusBar(null, false, null, false);
                                        } else if (!VolumeReceiver.canReceive())
                                            volumeReceiver.onVolumeChanged();
                                    } else setStatusBar(null, false, null, true);

                                    if (StaticUtils.shouldUseCompatNotifications(this)) {
                                        for (NotificationData notification : notifications) {
                                            Intent intent = new Intent(NotificationsIconData.ACTION_NOTIFICATION_REMOVED);
                                            intent.putExtra(NotificationsIconData.EXTRA_NOTIFICATION, notification);
                                            sendBroadcast(intent);
                                        }

                                        notifications.clear();
                                    }
                                } else setStatusBar(null, false, null, false);
                            }
                            return;
                        }

                        Boolean isFullscreen = activityData.getBooleanPreference(this, AppData.PreferenceIdentifier.FULLSCREEN);

                        if (packageManager != null) {
                            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
                            homeIntent.addCategory(Intent.CATEGORY_HOME);
                            ResolveInfo homeInfo = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY);

                            if (homeInfo != null && packageName.toString().matches(homeInfo.activityInfo.packageName)) {
                                setStatusBar(null, true, isFullscreen, false);
                                notificationManager.cancel(NOTIFICATION_ID);
                                return;
                            }
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
        if (volumeReceiver != null) unregisterReceiver(volumeReceiver);
        if (notificationManager != null) notificationManager.cancel(NOTIFICATION_ID);
        super.onDestroy();
    }

    private static class VolumeReceiver extends BroadcastReceiver {

        private SoftReference<AccessibilityService> reference;
        private Handler handler;
        private Runnable runnable;

        private VolumeReceiver(AccessibilityService service) {
            reference = new SoftReference<>(service);
            handler = new Handler();
            runnable = new Runnable() {
                @Override
                public void run() {
                    AccessibilityService service = reference.get();
                    if (service != null) {
                        Status.showDebug(service, "Volume callback called", Toast.LENGTH_SHORT);
                        service.setStatusBar(null, false, null, false);
                    }
                }
            };
        }

        @Override
        public void onReceive(Context context, Intent intent) {
            Status.showDebug(context, intent.getExtras().toString(), Toast.LENGTH_SHORT);
            onVolumeChanged();
        }

        private void onVolumeChanged() {
            AccessibilityService service = reference.get();
            if (service != null && shouldHideOnVolume(service)) {
                Status.showDebug(service, "Volume callback added", Toast.LENGTH_SHORT);
                service.setStatusBar(null, false, null, true);
                handler.removeCallbacks(runnable);

                handler.postDelayed(runnable, 3000);
            }
        }

        private void cancel() {
            AccessibilityService service = reference.get();
            if (service != null)
                Status.showDebug(service, "Volume callback removed", Toast.LENGTH_SHORT);
            handler.removeCallbacks(runnable);
        }

        private static boolean canReceive() {
            return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && Build.VERSION.SDK_INT < Build.VERSION_CODES.M;
        }
    }

    public static boolean shouldHideOnVolume(Context context) {
        Boolean isVolumeHidden = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_HIDE_ON_VOLUME);
        return (isVolumeHidden == null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) || (isVolumeHidden != null && isVolumeHidden);
    }
}
