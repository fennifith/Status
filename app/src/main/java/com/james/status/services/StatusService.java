package com.james.status.services;

import android.app.KeyguardManager;
import android.app.Notification;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.james.status.data.icon.AirplaneModeIconData;
import com.james.status.data.icon.AlarmIconData;
import com.james.status.data.icon.BatteryIconData;
import com.james.status.data.icon.BluetoothIconData;
import com.james.status.data.icon.DataIconData;
import com.james.status.data.icon.IconData;
import com.james.status.data.icon.NetworkIconData;
import com.james.status.data.icon.RingerIconData;
import com.james.status.data.icon.TimeIconData;
import com.james.status.data.icon.WifiIconData;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

import java.util.ArrayList;
import java.util.List;

public class StatusService extends Service {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_STOP = "com.james.status.ACTION_STOP",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
            ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED",
            ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED",
            EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION",
            EXTRA_NOTIFICATION_KEY = "com.james.status.EXTRA_NOTIFICATION_KEY",
            EXTRA_COLOR = "com.james.status.EXTRA_COLOR",
            EXTRA_SYSTEM_FULLSCREEN = "com.james.status.EXTRA_SYSTEM_FULLSCREEN",
            EXTRA_FULLSCREEN = "com.james.status.EXTRA_FULLSCREEN",
            EXTRA_PACKAGE_NAME = "com.james.status.EXTRA_PACKAGE_NAME";

    private StatusView statusView;
    private View fullscreenView;

    private KeyguardManager keyguardManager;
    private WindowManager windowManager;

    @Override
    public void onCreate() {
        super.onCreate();

        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);
        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);

        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) stopSelf();
        else setUp();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (action == null) return START_STICKY;
        switch (action) {
            case ACTION_START:
                setUp();
                break;
            case ACTION_STOP:
                windowManager.removeView(statusView);
                statusView = null;
                stopSelf();
                break;
            case ACTION_UPDATE:
                if (statusView != null) {
                    statusView.setLockscreen(keyguardManager.isKeyguardLocked());

                    Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
                    if ((isStatusColorAuto == null || isStatusColorAuto) && intent.hasExtra(EXTRA_COLOR))
                        statusView.setColor(intent.getIntExtra(EXTRA_COLOR, Color.BLACK));

                    statusView.setSystemShowing(intent.getBooleanExtra(EXTRA_SYSTEM_FULLSCREEN, statusView.isSystemShowing()));
                    statusView.setFullscreen(intent.getBooleanExtra(EXTRA_FULLSCREEN, isFullscreen()));
                }
                break;
            case ACTION_NOTIFICATION_ADDED:
                statusView.addNotification(intent.getStringExtra(EXTRA_NOTIFICATION_KEY), (Notification) intent.getParcelableExtra(EXTRA_NOTIFICATION), intent.getStringExtra(EXTRA_PACKAGE_NAME));
                break;
            case ACTION_NOTIFICATION_REMOVED:
                statusView.removeNotification(intent.getStringExtra(EXTRA_NOTIFICATION_KEY));
                break;
        }
        return START_STICKY;
    }


    public void setUp() {
        if (statusView == null || statusView.getParent() == null) {
            if (statusView != null) windowManager.removeView(statusView);
            statusView = new StatusView(this);

            WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
            params.gravity = Gravity.TOP;

            windowManager.addView(statusView, params);
        }

        statusView.setUp();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
            intent.setClass(this, NotificationService.class);
            startService(intent);
        }

        if (fullscreenView == null || fullscreenView.getParent() == null) {
            WindowManager.LayoutParams params = new WindowManager.LayoutParams(1, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSPARENT);
            params.gravity = Gravity.START | Gravity.TOP;
            fullscreenView = new View(this);

            windowManager.addView(fullscreenView, params);

            fullscreenView.getViewTreeObserver().addOnGlobalLayoutListener(new ViewTreeObserver.OnGlobalLayoutListener() {
                @Override
                public void onGlobalLayout() {
                    if (statusView != null && fullscreenView != null) {
                        Point size = new Point();
                        windowManager.getDefaultDisplay().getSize(size);
                        statusView.setFullscreen(fullscreenView.getMeasuredHeight() == size.y);
                    }
                }
            });
        }

        List<IconData> icons = new ArrayList<>();

        Boolean showClock = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_CLOCK);
        if (showClock == null || showClock) icons.add(new TimeIconData(this));

        Boolean battery = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_BATTERY_ICON);
        if (battery == null || battery) icons.add(new BatteryIconData(this));

        Boolean network = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_NETWORK_ICON);
        if (network == null || network) icons.add(new NetworkIconData(this));

        Boolean data = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_DATA);
        if (data == null || data) icons.add(new DataIconData(this));

        Boolean wifi = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_WIFI_ICON);
        if (wifi == null || wifi) icons.add(new WifiIconData(this));

        Boolean bluetooth = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_BLUETOOTH_ICON);
        if (bluetooth == null || bluetooth) icons.add(new BluetoothIconData(this));

        Boolean airplane = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_AIRPLANE_MODE_ICON);
        if (airplane == null || airplane) icons.add(new AirplaneModeIconData(this));

        Boolean alarm = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_ALARM_ICON);
        if ((alarm == null || alarm) && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            icons.add(new AlarmIconData(this));

        Boolean ringer = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_RINGER_ICON);
        if (ringer == null || ringer) icons.add(new RingerIconData(this));

        statusView.setIcons(icons);
        statusView.register();

        if (StaticUtils.isAccessibilityServiceRunning(this)) {
            Intent intent = new Intent(AccessibilityService.ACTION_GET_COLOR);
            intent.setClass(this, AccessibilityService.class);
            startService(intent);
        }
    }

    public boolean isFullscreen() {
        if (statusView != null && fullscreenView != null) {
            Point size = new Point();
            windowManager.getDefaultDisplay().getSize(size);
            return fullscreenView.getMeasuredHeight() == size.y;
        } else return false;
    }

    @Override
    public void onDestroy() {
        if (fullscreenView != null) {
            windowManager.removeView(fullscreenView);
            fullscreenView = null;
        }

        if (statusView != null) {
            statusView.unregister();
            windowManager.removeView(statusView);
            statusView = null;
        }

        super.onDestroy();
    }
}
