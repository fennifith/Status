package com.james.status.services;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.service.notification.StatusBarNotification;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

import java.util.ArrayList;
import java.util.Collections;

public class StatusService extends ViewService implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_STOP = "com.james.status.ACTION_STOP",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
            ACTION_NOTIFICATION = "com.james.status.ACTION_NOTIFICATION",
            EXTRA_NOTIFICATIONS = "com.james.status.EXTRA_NOTIFICATIONS",
            EXTRA_COLOR = "com.james.status.EXTRA_COLOR";

    StatusView statusView;

    ConnectivityManager connectivityManager;
    TelephonyManager telephonyManager;
    WifiManager wifiManager;
    KeyguardManager keyguardManager;

    BatteryReceiver batteryReceiver;
    NetworkReceiver networkReceiver;
    WifiReceiver wifiReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);

        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) stopSelf();

        keyguardManager = (KeyguardManager) getSystemService(Context.KEYGUARD_SERVICE);
        connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
        telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
        wifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);

        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));

        networkReceiver = new NetworkReceiver();
        telephonyManager.listen(networkReceiver, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);

        wifiReceiver = new WifiReceiver();
        registerReceiver(wifiReceiver, new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (action == null) return START_STICKY;
        switch (action) {
            case ACTION_START:
                if (statusView != null) removeView(statusView);
                statusView = new StatusView(this);
                WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticUtils.getStatusBarMargin(this));
                params.gravity = Gravity.TOP;
                addView(statusView, params);
                break;
            case ACTION_STOP:
                removeView(statusView);
                statusView = null;
                stopSelf();
                break;
            case ACTION_UPDATE:
                if (statusView != null) {
                    statusView.setLockscreen(keyguardManager.isKeyguardLocked());
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

    @Override
    public void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        unregisterReceiver(batteryReceiver);
        unregisterReceiver(wifiReceiver);
        super.onDestroy();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String s) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) stopSelf();
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (statusView != null) statusView.setBattery(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0), intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0));
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
                int state = wifiManager.getWifiState();
                statusView.setWifiConnected(state != WifiManager.WIFI_STATE_DISABLED && state != WifiManager.WIFI_STATE_DISABLING && state != WifiManager.WIFI_STATE_UNKNOWN);
                statusView.setWifiStrength(WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4));
            }
        }
    }
}
