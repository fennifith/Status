package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;

public class BatteryIconData extends IconData {

    BatteryReceiver receiver;

    public BatteryIconData(Context context) {
        super(context);
        receiver = new BatteryReceiver();
    }

    @Override
    public boolean hasText() {
        Boolean isBatteryPercent = getBooleanPreference(PreferenceIdentifier.TEXT_VISIBILITY);
        return isBatteryPercent != null && isBatteryPercent;
    }

    @Override
    public String getFakeText() {
        return "100%";
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(Intent.ACTION_BATTERY_CHANGED);
    }

    @Override
    public void register() {
        Intent intent = getContext().registerReceiver(receiver, getIntentFilter());

        if (intent != null) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);

            int iconLevel = (int) (((float) level / scale) * 7);

            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                iconLevel += 7;

            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(iconLevel), getContext().getTheme()));

            if (hasText()) onTextUpdate(String.valueOf(level) + "%");
        }
    }

    @Override
    public void unregister() {
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{
                R.drawable.ic_battery_alert,
                R.drawable.ic_battery_20,
                R.drawable.ic_battery_30,
                R.drawable.ic_battery_50,
                R.drawable.ic_battery_60,
                R.drawable.ic_battery_80,
                R.drawable.ic_battery_90,
                R.drawable.ic_battery_full,
                R.drawable.ic_battery_charging_20,
                R.drawable.ic_battery_charging_30,
                R.drawable.ic_battery_charging_50,
                R.drawable.ic_battery_charging_60,
                R.drawable.ic_battery_charging_80,
                R.drawable.ic_battery_charging_90,
                R.drawable.ic_battery_charging_full
        };
    }

    public class BatteryReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);

            int iconLevel = (int) (((float) level / scale) * 7);

            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                iconLevel += 7;

            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(iconLevel), getContext().getTheme()));

            if (hasText()) onTextUpdate(String.valueOf((level / scale) * 100) + "%");
        }
    }
}
