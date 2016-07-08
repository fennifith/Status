package com.james.status.services;

import android.app.AlarmManager;
import android.app.KeyguardManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.graphics.PixelFormat;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.IBinder;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.WindowManager;

import com.james.status.utils.PreferenceUtils;
import com.james.status.views.StatusView;

import java.util.ArrayList;

public class StatusService extends Service {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_STOP = "com.james.status.ACTION_STOP",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
            ACTION_NOTIFICATION = "com.james.status.ACTION_NOTIFICATION",
            EXTRA_NOTIFICATIONS = "com.james.status.EXTRA_NOTIFICATIONS",
            EXTRA_COLOR = "com.james.status.EXTRA_COLOR";

    private StatusView statusView;

    private AlarmManager alarmManager;
    private WifiManager wifiManager;
    private TelephonyManager telephonyManager;
    private KeyguardManager keyguardManager;
    private WindowManager windowManager;

    private AlarmReceiver alarmReceiver;
    private AirplaneModeReceiver airplaneModeReceiver;
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

        alarmReceiver = new AlarmReceiver();
        registerReceiver(alarmReceiver, new IntentFilter(AlarmManager.ACTION_NEXT_ALARM_CLOCK_CHANGED));

        airplaneModeReceiver = new AirplaneModeReceiver();
        registerReceiver(airplaneModeReceiver, new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED));

        networkReceiver = new NetworkReceiver();
        telephonyManager.listen(networkReceiver, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));

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
                    if (isStatusColorAuto == null || isStatusColorAuto)
                        statusView.setColor(intent.getIntExtra(EXTRA_COLOR, Color.BLACK));
                }
                break;
            case ACTION_NOTIFICATION:
                ArrayList<StatusBarNotification> notifications = new ArrayList<>();
                for (Parcelable parcelable : intent.getParcelableArrayListExtra(EXTRA_NOTIFICATIONS)) {
                    notifications.add((StatusBarNotification) parcelable);
                }

                if (statusView != null) statusView.setNotifications(notifications);
                break;
        }
        return START_STICKY;
    }


    public void setUp() {
        if (statusView != null) windowManager.removeView(statusView);
        statusView = new StatusView(this);

        WindowManager.LayoutParams params = new WindowManager.LayoutParams(WindowManager.LayoutParams.WRAP_CONTENT, WindowManager.LayoutParams.WRAP_CONTENT);
        params.gravity = Gravity.TOP;
        params.flags = WindowManager.LayoutParams.FLAG_NOT_FOCUSABLE | WindowManager.LayoutParams.FLAG_LAYOUT_IN_SCREEN;
        params.type = WindowManager.LayoutParams.TYPE_SYSTEM_OVERLAY;
        params.format = PixelFormat.TRANSLUCENT;

        windowManager.addView(statusView, params);

        statusView.setAlarm(alarmManager.getNextAlarmClock() != null);
        statusView.setWifiStrength(WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4));
        int state = wifiManager.getWifiState();
        statusView.setWifiConnected(state != WifiManager.WIFI_STATE_DISABLED && state != WifiManager.WIFI_STATE_DISABLING && state != WifiManager.WIFI_STATE_UNKNOWN);

        Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
        intent.setClass(this, NotificationService.class);
        startService(intent);
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(alarmReceiver);
        unregisterReceiver(airplaneModeReceiver);
        unregisterReceiver(wifiReceiver);
        unregisterReceiver(batteryReceiver);

        if (statusView != null) {
            windowManager.removeView(statusView);
            statusView = null;
        }

        super.onDestroy();
    }

    private class AlarmReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null) statusView.setAlarm(alarmManager.getNextAlarmClock() != null);
        }
    }

    private class AirplaneModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null)
                statusView.setAirplaneMode(intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false));
        }
    }

    private class NetworkReceiver extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (statusView != null) statusView.setSignalStrength(signalStrength.getGsmSignalStrength());
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null) {
                statusView.setWifiStrength(WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4));

                int state = wifiManager.getWifiState();
                statusView.setWifiConnected(state != WifiManager.WIFI_STATE_DISABLED && state != WifiManager.WIFI_STATE_DISABLING && state != WifiManager.WIFI_STATE_UNKNOWN);
            }
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
