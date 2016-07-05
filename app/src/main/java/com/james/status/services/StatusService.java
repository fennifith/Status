package com.james.status.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.PixelFormat;
import android.os.BatteryManager;
import android.util.TypedValue;
import android.view.Gravity;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Toast;

import com.james.status.utils.StaticUtils;
import com.james.status.views.StatusView;

public class StatusService extends WindowModifierService {

    public static final String ACTION_START = "com.james.status.ACTION_START";

    StatusView statusView;

    BatteryReceiver batteryReceiver;

    @Override
    public void onCreate() {
        super.onCreate();

        if (statusView != null) removeView(statusView);
        statusView = new StatusView(this);
        WindowManager.LayoutParams params = new WindowManager.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, StaticUtils.getStatusBarMargin(this));
        params.gravity = Gravity.TOP;
        addView(statusView, params);

        batteryReceiver = new BatteryReceiver();
        registerReceiver(batteryReceiver, new IntentFilter(Intent.ACTION_BATTERY_CHANGED));
    }

    @Override
    public void onDestroy() {
        unregisterReceiver(batteryReceiver);
        super.onDestroy();
    }

    private class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            statusView.setBattery(intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0), intent.getIntExtra(BatteryManager.EXTRA_SCALE, 0), intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1));
        }
    }
}
