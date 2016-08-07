package com.james.status.services;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.graphics.Point;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Build;
import android.os.IBinder;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.View;
import android.view.ViewTreeObserver;
import android.view.WindowManager;

import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

import java.util.ArrayList;

public class StatusService extends Service {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_STOP = "com.james.status.ACTION_STOP",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
            ACTION_NOTIFICATION = "com.james.status.ACTION_NOTIFICATION",
            ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED",
            ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED",
            EXTRA_NOTIFICATIONS = "com.james.status.EXTRA_NOTIFICATIONS",
            EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION",
            EXTRA_COLOR = "com.james.status.EXTRA_COLOR",
            EXTRA_SYSTEM_FULLSCREEN = "com.james.status.EXTRA_SYSTEM_FULLSCREEN",
            EXTRA_FULLSCREEN = "com.james.status.EXTRA_FULLSCREEN";

    private StatusView statusView;
    private View fullscreenView;

    private AlarmManager alarmManager;
    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private KeyguardManager keyguardManager;
    private WindowManager windowManager;

    private AlarmReceiver alarmReceiver;
    private AirplaneModeReceiver airplaneModeReceiver;
    private BluetoothReceiver bluetoothReceiver;
    private NetworkReceiver networkReceiver;
    private WifiReceiver wifiReceiver;
    private BatteryReceiver batteryReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        windowManager = (WindowManager) getSystemService(Context.WINDOW_SERVICE);

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        alarmManager = (AlarmManager) getSystemService(Context.ALARM_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            alarmReceiver = new AlarmReceiver();
            registerReceiver(alarmReceiver, new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED));
        }

        airplaneModeReceiver = new AirplaneModeReceiver();
        registerReceiver(airplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

        bluetoothReceiver = new BluetoothReceiver();
        registerReceiver(bluetoothReceiver, new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED));

        networkReceiver = new NetworkReceiver();
        telephonyManager.listen(networkReceiver, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION));

        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

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

                    if (intent.hasExtra(EXTRA_SYSTEM_FULLSCREEN))
                        statusView.setSystemShowing(intent.getBooleanExtra(EXTRA_SYSTEM_FULLSCREEN, false));

                    if (intent.hasExtra(EXTRA_FULLSCREEN))
                        statusView.setFullscreen(intent.getBooleanExtra(EXTRA_FULLSCREEN, false));
                }
                break;
            case ACTION_NOTIFICATION:
                ArrayList<StatusBarNotification> notifications = new ArrayList<>();
                for (Parcelable parcelable : intent.getParcelableArrayListExtra(EXTRA_NOTIFICATIONS)) {
                    notifications.add((StatusBarNotification) parcelable);
                }

                if (statusView != null) statusView.setNotifications(notifications);
                break;
            case ACTION_NOTIFICATION_ADDED:
                statusView.addNotification((StatusBarNotification) intent.getParcelableExtra(EXTRA_NOTIFICATION));
                break;
            case ACTION_NOTIFICATION_REMOVED:
                statusView.removeNotification((StatusBarNotification) intent.getParcelableExtra(EXTRA_NOTIFICATION));
                break;
        }
        return START_STICKY;
    }


    public void setUp() {
        if (statusView != null) windowManager.removeView(statusView);
        statusView = new StatusView(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN, PixelFormat.TRANSLUCENT);
        params.gravity = Gravity.TOP;

        windowManager.addView(statusView, params);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            statusView.setAlarm(alarmManager.getNextAlarmClock() != null);

        int bluetoothState = StaticUtils.getBluetoothState(this);
        statusView.setBluetooth(bluetoothState != BluetoothAdapter.STATE_OFF, bluetoothState == BluetoothAdapter.STATE_CONNECTED);

        statusView.setWifiConnected(wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED);

        int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4);
        statusView.setWifiStrength(level);

        Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
        intent.setClass(this, NotificationService.class);
        startService(intent);

        params = new WindowManager.LayoutParams(1, WindowManager.LayoutParams.MATCH_PARENT, WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY, WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE, PixelFormat.TRANSPARENT);
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

    @Override
    public void onDestroy() {
        unregisterReceiver(alarmReceiver);
        unregisterReceiver(airplaneModeReceiver);
        unregisterReceiver(bluetoothReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(batteryReceiver);

        if (fullscreenView != null) {
            windowManager.removeView(fullscreenView);
            fullscreenView = null;
        }

        if (statusView != null) {
            windowManager.removeView(statusView);
            statusView = null;
        }

        super.onDestroy();
    }

    private class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                statusView.setAlarm(alarmManager.getNextAlarmClock() != null);
        }
    }

    private class AirplaneModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null)
                statusView.setAirplaneMode(intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false));
        }
    }

    private class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (statusView != null)
                statusView.setBluetooth(state != BluetoothAdapter.STATE_OFF, state == BluetoothAdapter.STATE_CONNECTED);
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            NetworkInfo info = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (statusView != null && info != null) {
                statusView.setWifiConnected(info.isConnected());

                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4);
                statusView.setWifiStrength(level);
            }
        }
    }

    private class NetworkReceiver extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (statusView != null)
                statusView.setSignalStrength(signalStrength.getGsmSignalStrength());
        }
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null)
                statusView.setBattery(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0), intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0));
        }
    }
}
