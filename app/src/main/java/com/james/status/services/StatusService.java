package com.james.status.services;

import android.app.KeyguardManager;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.net.ConnectivityManager;
import android.net.Network;
import android.net.wifi.SupplicantState;
import android.net.wifi.WifiManager;
import android.os.BatteryManager;
import android.preference.PreferenceManager;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;

import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

public class StatusService extends ViewService implements SharedPreferences.OnSharedPreferenceChangeListener {

    public static final String
            ACTION_START = "com.james.status.ACTION_START",
            ACTION_UPDATE = "com.james.status.ACTION_UPDATE",
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

        if (statusView != null) removeView(statusView);
        statusView = new StatusView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticUtils.getStatusBarMargin(this));
        params.gravity = Gravity.TOP;
        addView(statusView, params);

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
                break;
            case ACTION_UPDATE:
                statusView.setLockscreen(keyguardManager.isKeyguardLocked());
                statusView.setColor(intent.getIntExtra(EXTRA_COLOR, Color.BLACK));
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
            statusView.setBattery(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0), intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0));
        }
    }

    private class NetworkReceiver extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            statusView.setSignalStrength(signalStrength.getGsmSignalStrength());
        }
    }

    private class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {

            SupplicantState state = wifiManager.getConnectionInfo().getSupplicantState();
            statusView.setWifiConnected(state != SupplicantState.DISCONNECTED && state != SupplicantState.DORMANT && state != SupplicantState.UNINITIALIZED);
            statusView.setWifiStrength(WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4));
        }
    }
}
