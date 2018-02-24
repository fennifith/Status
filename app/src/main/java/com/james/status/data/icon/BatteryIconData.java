package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.os.BatteryManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class BatteryIconData extends IconData {

    private BatteryReceiver receiver;

    public BatteryIconData(Context context) {
        super(context);
        receiver = new BatteryReceiver(this);
    }

    @Override
    public boolean canHazText() {
        return true;
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

            int iconLevel = (int) (((float) level / scale) * 6) + 1;

            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                iconLevel += 7;

            onIconUpdate(iconLevel);

            if (hasText())
                onTextUpdate(String.valueOf((int) (((double) level / scale) * 100)) + "%");
        }
    }

    @Override
    public void unregister() {
        getContext().unregisterReceiver(receiver);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_battery);
    }

    @Override
    public int getIconStyleSize() {
        return 15;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
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
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_outline),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_battery_outline_alert",
                                "ic_battery_outline_20",
                                "ic_battery_outline_30",
                                "ic_battery_outline_50",
                                "ic_battery_outline_60",
                                "ic_battery_outline_80",
                                "ic_battery_outline_90",
                                "ic_battery_outline_full",
                                "ic_battery_outline_charging_20",
                                "ic_battery_outline_charging_30",
                                "ic_battery_outline_charging_50",
                                "ic_battery_outline_charging_60",
                                "ic_battery_outline_charging_80",
                                "ic_battery_outline_charging_90",
                                "ic_battery_outline_charging_full"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_sideways),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_battery_sideways_alert",
                                "ic_battery_sideways_20",
                                "ic_battery_sideways_40",
                                "ic_battery_sideways_40",
                                "ic_battery_sideways_60",
                                "ic_battery_sideways_60",
                                "ic_battery_sideways_80",
                                "ic_battery_sideways_full",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_charging",
                                "ic_battery_sideways_full"
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_stock),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_battery_stock_alert,
                                R.drawable.ic_battery_stock_20,
                                R.drawable.ic_battery_stock_30,
                                R.drawable.ic_battery_stock_50,
                                R.drawable.ic_battery_stock_60,
                                R.drawable.ic_battery_stock_80,
                                R.drawable.ic_battery_stock_90,
                                R.drawable.ic_battery_stock_full,
                                R.drawable.ic_battery_stock_charging_20,
                                R.drawable.ic_battery_stock_charging_30,
                                R.drawable.ic_battery_stock_charging_50,
                                R.drawable.ic_battery_stock_charging_60,
                                R.drawable.ic_battery_stock_charging_80,
                                R.drawable.ic_battery_stock_charging_90,
                                R.drawable.ic_battery_stock_charging_full
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_circle),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_battery_circle_alert,
                                R.drawable.ic_battery_circle_20,
                                R.drawable.ic_battery_circle_30,
                                R.drawable.ic_battery_circle_50,
                                R.drawable.ic_battery_circle_60,
                                R.drawable.ic_battery_circle_80,
                                R.drawable.ic_battery_circle_90,
                                R.drawable.ic_battery_circle_full,
                                R.drawable.ic_battery_circle_charging_20,
                                R.drawable.ic_battery_circle_charging_30,
                                R.drawable.ic_battery_circle_charging_50,
                                R.drawable.ic_battery_circle_charging_60,
                                R.drawable.ic_battery_circle_charging_80,
                                R.drawable.ic_battery_circle_charging_90,
                                R.drawable.ic_battery_circle_charging_full
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_retro),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_battery_retro_alert,
                                R.drawable.ic_battery_retro_20,
                                R.drawable.ic_battery_retro_30,
                                R.drawable.ic_battery_retro_50,
                                R.drawable.ic_battery_retro_60,
                                R.drawable.ic_battery_retro_80,
                                R.drawable.ic_battery_retro_90,
                                R.drawable.ic_battery_retro_full,
                                R.drawable.ic_battery_retro_20,
                                R.drawable.ic_battery_retro_30,
                                R.drawable.ic_battery_retro_50,
                                R.drawable.ic_battery_retro_60,
                                R.drawable.ic_battery_retro_80,
                                R.drawable.ic_battery_retro_90,
                                R.drawable.ic_battery_retro_full
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_circle_outline),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_battery_circle_outline_alert,
                                R.drawable.ic_battery_circle_outline_20,
                                R.drawable.ic_battery_circle_outline_30,
                                R.drawable.ic_battery_circle_outline_50,
                                R.drawable.ic_battery_circle_outline_60,
                                R.drawable.ic_battery_circle_outline_80,
                                R.drawable.ic_battery_circle_outline_90,
                                R.drawable.ic_battery_circle_outline_full,
                                R.drawable.ic_battery_circle_outline_20,
                                R.drawable.ic_battery_circle_outline_30,
                                R.drawable.ic_battery_circle_outline_50,
                                R.drawable.ic_battery_circle_outline_60,
                                R.drawable.ic_battery_circle_outline_80,
                                R.drawable.ic_battery_circle_outline_90,
                                R.drawable.ic_battery_circle_outline_full
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_battery_alert),
                getContext().getString(R.string.icon_battery_0_15),
                getContext().getString(R.string.icon_battery_15_30),
                getContext().getString(R.string.icon_battery_30_45),
                getContext().getString(R.string.icon_battery_45_60),
                getContext().getString(R.string.icon_battery_60_75),
                getContext().getString(R.string.icon_battery_75_90),
                getContext().getString(R.string.icon_battery_full),
                getContext().getString(R.string.icon_battery_charging_0_15),
                getContext().getString(R.string.icon_battery_charging_15_30),
                getContext().getString(R.string.icon_battery_charging_30_45),
                getContext().getString(R.string.icon_battery_charging_45_60),
                getContext().getString(R.string.icon_battery_charging_60_75),
                getContext().getString(R.string.icon_battery_charging_75_90),
                getContext().getString(R.string.icon_battery_charging_full)
        };
    }

    static class BatteryReceiver extends IconUpdateReceiver<BatteryIconData> {

        private BatteryReceiver(BatteryIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(BatteryIconData icon, Intent intent) {
            int level = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, 0);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, 1);
            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, 0);

            int iconLevel = (int) (((float) level / scale) * 6) + 1;

            if (status == BatteryManager.BATTERY_STATUS_CHARGING || status == BatteryManager.BATTERY_STATUS_FULL)
                iconLevel += 7;

            icon.onIconUpdate(iconLevel);

            if (icon.hasText())
                icon.onTextUpdate(String.valueOf((int) (((double) level / scale) * 100)) + "%");
        }
    }
}
