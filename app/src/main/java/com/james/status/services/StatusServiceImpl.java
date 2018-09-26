package com.james.status.services;

import android.app.AlarmManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.ComponentName;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.SystemClock;
import android.support.v4.app.NotificationCompat;
import android.support.v4.app.TaskStackBuilder;
import android.support.v4.content.ContextCompat;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.james.status.BuildConfig;
import com.james.status.R;
import com.james.status.activities.AppSettingActivity;
import com.james.status.activities.MainActivity;
import com.james.status.data.AppData;
import com.james.status.data.AppPreferenceData;
import com.james.status.data.NotificationData;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.AirplaneModeIconData;
import com.james.status.data.icon.AlarmIconData;
import com.james.status.data.icon.BatteryIconData;
import com.james.status.data.icon.BluetoothIconData;
import com.james.status.data.icon.CarrierIconData;
import com.james.status.data.icon.DataIconData;
import com.james.status.data.icon.GpsIconData;
import com.james.status.data.icon.HeadphoneIconData;
import com.james.status.data.icon.IconData;
import com.james.status.data.icon.NetworkIconData;
import com.james.status.data.icon.NfcIconData;
import com.james.status.data.icon.NotificationsIconData;
import com.james.status.data.icon.OrientationIconData;
import com.james.status.data.icon.RingerIconData;
import com.james.status.data.icon.TimeIconData;
import com.james.status.data.icon.WifiIconData;
import com.james.status.receivers.ActivityFullScreenSettingReceiver;
import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

import java.util.ArrayList;
import java.util.List;

public class StatusServiceImpl {

    public static final String ACTION_CREATE = "com.james.status.ACTION_CREATE";
    public static final String ACTION_START = "com.james.status.ACTION_START";
    public static final String ACTION_STOP = "com.james.status.ACTION_STOP";
    public static final String ACTION_UPDATE = "com.james.status.ACTION_UPDATE";
    public static final String EXTRA_KEEP_OLD = "com.james.status.EXTRA_KEEP_OLD";
    public static final String EXTRA_COLOR = "com.james.status.EXTRA_COLOR";
    public static final String EXTRA_IS_SYSTEM_FULLSCREEN = "com.james.status.EXTRA_IS_SYSTEM_FULLSCREEN";
    public static final String EXTRA_IS_FULLSCREEN = "com.james.status.EXTRA_IS_FULLSCREEN";
    public static final String EXTRA_IS_TRANSPARENT = "com.james.status.EXTRA_IS_TRANSPARENT";
    public static final String EXTRA_PACKAGE = "com.james.status.EXTRA_PACKAGE";
    public static final String EXTRA_ACTIVITY = "com.james.status.EXTRA_ACTIVITY";

    private static final int ID_FOREGROUND = 682;

    private StatusView statusView;
    private View fullscreenView;

    private WindowManager windowManager;

    private String packageName;
    private AppData.ActivityData activityData;
    private AppPreferenceData activityPreference;

    private Service service;

    public StatusServiceImpl(Service service) {
        this.service = service;
    }

    public void onCreate() {
        windowManager = (WindowManager) service.getSystemService(Context.WINDOW_SERVICE);

        if (PreferenceData.STATUS_ENABLED.getValue(service))
            setUp(false);
    }

    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!(boolean) PreferenceData.STATUS_ENABLED.getValue(service)) {
            onDestroy();
            disable(service);
            return Service.START_NOT_STICKY;
        }

        if (intent == null) return Service.START_STICKY;
        String action = intent.getAction();
        if (action == null) return Service.START_STICKY;
        switch (action) {
            case ACTION_CREATE:
                if (statusView == null)
                    setUp(false);
                break;
            case ACTION_START:
                setUp(intent.getBooleanExtra(EXTRA_KEEP_OLD, false));
                break;
            case ACTION_STOP:
                onDestroy();
                disable(service);
                break;
            case ACTION_UPDATE:
                if (statusView != null) {
                    if (intent.hasExtra(EXTRA_IS_TRANSPARENT) && intent.getBooleanExtra(EXTRA_IS_TRANSPARENT, false))
                        statusView.setTransparent();
                    else if (intent.hasExtra(EXTRA_COLOR))
                        statusView.setColor(intent.getIntExtra(EXTRA_COLOR, Color.BLACK));

                    statusView.setSystemShowing(intent.getBooleanExtra(EXTRA_IS_SYSTEM_FULLSCREEN, statusView.isSystemShowing()));
                    statusView.setFullscreen(intent.getBooleanExtra(EXTRA_IS_FULLSCREEN, isFullscreen()));

                    if (intent.hasExtra(EXTRA_PACKAGE) && intent.hasExtra(EXTRA_ACTIVITY)) {
                        if (PreferenceData.STATUS_PERSISTENT_NOTIFICATION.getValue(service)) {
                            packageName = intent.getStringExtra(EXTRA_PACKAGE);
                            activityData = intent.getParcelableExtra(EXTRA_ACTIVITY);
                            if (activityData != null)
                                activityPreference = new AppPreferenceData(service, activityData.packageName + "/" + activityData.name);
                            else activityPreference = null;

                            startForeground(packageName, activityData);
                        } else service.stopForeground(true);
                    }
                }
                return Service.START_STICKY;
        }

        if (PreferenceData.STATUS_PERSISTENT_NOTIFICATION.getValue(service)) {
            if (packageName != null && activityData != null)
                startForeground(packageName, activityData);
            else {
                Intent contentIntent = new Intent(service, MainActivity.class);
                contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

                TaskStackBuilder contentStackBuilder = TaskStackBuilder.create(service);
                contentStackBuilder.addParentStack(MainActivity.class);
                contentStackBuilder.addNextIntent(contentIntent);

                service.startForeground(ID_FOREGROUND, new NotificationCompat.Builder(service)
                        .setSmallIcon(R.drawable.ic_notification)
                        .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                        .setContentTitle(service.getString(R.string.app_name))
                        .setContentIntent(contentStackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT))
                        .build()
                );
            }
        } else service.stopForeground(true);

        return Service.START_STICKY;
    }

    private void startForeground(String packageName, AppData.ActivityData activityData) {
        Intent contentIntent = new Intent(service, MainActivity.class);
        contentIntent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);

        TaskStackBuilder contentStackBuilder = TaskStackBuilder.create(service);
        contentStackBuilder.addParentStack(MainActivity.class);
        contentStackBuilder.addNextIntent(contentIntent);

        NotificationCompat.Builder builder = new NotificationCompat.Builder(service)
                .setSmallIcon(R.drawable.ic_notification)
                .setColor(ContextCompat.getColor(service, R.color.colorAccent))
                .setContentTitle(service.getString(R.string.app_name))
                .setContentText(activityData.name)
                .setSubText(packageName)
                .setContentIntent(contentStackBuilder.getPendingIntent(0, PendingIntent.FLAG_CANCEL_CURRENT));

        if (PreferenceData.STATUS_COLOR_AUTO.getValue(service)) {
            Intent colorIntent = new Intent(service, AppSettingActivity.class);
            colorIntent.putExtra(AppSettingActivity.EXTRA_COMPONENT, activityData.packageName + "/" + activityData.name);
            colorIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

            builder.addAction(R.drawable.ic_notification_color, service.getString(R.string.action_set_color), PendingIntent.getActivity(service, 0, colorIntent, 0));
        }

        boolean isFullscreen = activityPreference != null && activityPreference.isFullScreen(service);
        Intent visibleIntent = new Intent(service, ActivityFullScreenSettingReceiver.class);
        visibleIntent.putExtra(ActivityFullScreenSettingReceiver.EXTRA_COMPONENT, activityData.packageName + "/" + activityData.name);
        visibleIntent.putExtra(ActivityFullScreenSettingReceiver.EXTRA_FULLSCREEN, isFullscreen);

        builder.addAction(R.drawable.ic_notification_visible, service.getString(isFullscreen ? R.string.action_show_status : R.string.action_hide_status), PendingIntent.getBroadcast(service, 0, visibleIntent, PendingIntent.FLAG_CANCEL_CURRENT));

        Intent settingsIntent = new Intent(service, AppSettingActivity.class);
        settingsIntent.putExtra(AppSettingActivity.EXTRA_COMPONENT, activityData.packageName);
        settingsIntent.setFlags(Intent.FLAG_ACTIVITY_MULTIPLE_TASK);

        builder.addAction(R.drawable.ic_notification_settings, service.getString(R.string.action_app_settings), PendingIntent.getActivity(service, 0, settingsIntent, 0));

        service.startForeground(ID_FOREGROUND, builder.build());
    }


    /**
     * I can't remember why this is here or what it does, but it seems important.
     *
     * @param rootIntent a... root intent, possibly part of a plant
     */
    public void onTaskRemoved(Intent rootIntent) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT) {
            Intent intent = new Intent(ACTION_UPDATE);
            intent.setClass(service, StatusServiceImpl.class);

            PendingIntent pendingIntent = PendingIntent.getService(service.getApplicationContext(), 1, intent, PendingIntent.FLAG_ONE_SHOT);

            AlarmManager manager = (AlarmManager) service.getApplicationContext().getSystemService(Context.ALARM_SERVICE);
            if (manager != null)
                manager.set(AlarmManager.ELAPSED_REALTIME, SystemClock.elapsedRealtime() + 1000, pendingIntent);
        }
    }

    /**
     * Initializes the StatusView if it doesn't exist yet, sets listeners,
     * applies any changes to preferences/icons
     *
     * @param shouldKeepOld whether to reuse the old IconData instances
     */
    public void setUp(boolean shouldKeepOld) {
        if (statusView == null || statusView.getParent() == null) {
            if (statusView != null) {
                windowManager.removeView(statusView);
                statusView.unregister();
            }

            statusView = new StatusView(service);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, StaticUtils.getStatusBarHeight(service), WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;

            windowManager.addView(statusView, params);
        } else if (!shouldKeepOld)
            statusView.unregister();

        statusView.init();

        if (!shouldKeepOld) {
            if (fullscreenView == null || fullscreenView.getParent() == null) {
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSPARENT);
                params.gravity = Gravity.START | Gravity.TOP;
                fullscreenView = new View(service);

                windowManager.addView(fullscreenView, params);

                fullscreenView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                    @Override
                    public void onGlobalLayout() {
                        if (statusView != null && fullscreenView != null) {
                            if (activityPreference != null && activityPreference.isFullScreenIgnore(service))
                                return;

                            Point size = new Point();
                            windowManager.getDefaultDisplay().getSize(size);
                            statusView.setFullscreen(fullscreenView.getMeasuredHeight() == size.y);
                        }
                    }
                });
            }

            statusView.setIcons(getIcons(service));
            statusView.register();

            if (service instanceof StatusService)
                ((StatusService) service).sendNotifications();
        }

        if (StaticUtils.isAccessibilityServiceRunning(service)) {
            Intent intent = new Intent(AccessibilityService.ACTION_GET_COLOR);
            intent.setClass(service, AccessibilityService.class);
            service.startService(intent);
        }
    }

    public boolean isFullscreen() {
        if (statusView != null && fullscreenView != null) {
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            return fullscreenView.getMeasuredHeight() == size.y;
        } else return false;
    }

    public void onDestroy() {
        if (fullscreenView != null) {
            windowManager.removeView(fullscreenView);
            fullscreenView = null;
        }

        if (statusView != null) {
            if (statusView.isRegistered()) statusView.unregister();
            windowManager.removeView(statusView);
            statusView = null;
        }
    }

    public static List<IconData> getIcons(Context context) {
        List<IconData> icons = new ArrayList<>();
        icons.add(new NotificationsIconData(context));
        icons.add(new TimeIconData(context));
        icons.add(new BatteryIconData(context));

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_TELEPHONY) || BuildConfig.DEBUG) {
            icons.add(new NetworkIconData(context));
            icons.add(new CarrierIconData(context));
            icons.add(new DataIconData(context));
        }

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_WIFI) || BuildConfig.DEBUG)
            icons.add(new WifiIconData(context));

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_BLUETOOTH) || BuildConfig.DEBUG)
            icons.add(new BluetoothIconData(context));

        if (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_LOCATION_GPS) || BuildConfig.DEBUG)
            icons.add(new GpsIconData(context));

        icons.add(new AirplaneModeIconData(context));
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && (context.getPackageManager().hasSystemFeature(PackageManager.FEATURE_NFC) || BuildConfig.DEBUG))
            icons.add(new NfcIconData(context));

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP || BuildConfig.DEBUG)
            icons.add(new AlarmIconData(context));

        icons.add(new RingerIconData(context));
        icons.add(new HeadphoneIconData(context));
        icons.add(new OrientationIconData(context));

        return icons;
    }

    public void onNotificationAdded(String key, NotificationData notification) {
        if (statusView != null)
            statusView.sendMessage(NotificationsIconData.class, key, notification);
    }

    public void onNotificationRemoved(String key) {
        if (statusView != null)
            statusView.sendMessage(NotificationsIconData.class, key);
    }

    public static Class getCompatClass(Context context) {
        if (context instanceof StatusService || context instanceof StatusServiceCompat)
            return context.getClass(); //prevents issues disabling services during compatibility switch

        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2 && !((Boolean) PreferenceData.STATUS_NOTIFICATIONS_COMPAT.getValue(context))
                ? StatusService.class : StatusServiceCompat.class;
    }

    public static void start(Context context) {
        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, getCompatClass(context)),
                PackageManager.COMPONENT_ENABLED_STATE_ENABLED, PackageManager.DONT_KILL_APP);

        Intent intent = new Intent(ACTION_CREATE);
        intent.setClass(context, getCompatClass(context));
        context.startService(intent);
    }

    public static void stop(Context context) {
        Intent intent = new Intent(ACTION_STOP);
        intent.setClass(context, getCompatClass(context));
        context.startService(intent);
    }

    public static void disable(Context context) {
        Intent intent = new Intent(ACTION_STOP);
        intent.setClass(context, getCompatClass(context));
        context.stopService(intent);

        PackageManager pm = context.getPackageManager();
        pm.setComponentEnabledSetting(new ComponentName(context, getCompatClass(context)),
                PackageManager.COMPONENT_ENABLED_STATE_DISABLED, PackageManager.DONT_KILL_APP);
    }

}
