package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.wifi.WifiManager;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class WifiIconData extends IconData<WifiIconData.WifiReceiver> {

    WifiManager wifiManager;

    public WifiIconData(Context context) {
        super(context);
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
    }

    @Override
    public WifiReceiver getReceiver() {
        return new WifiReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    @Override
    public void register() {
        super.register();

        if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
            int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4);
            onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(level), getContext().getTheme()));
        } else onDrawableUpdate(null);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_wifi);
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = new ArrayList<>();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                R.drawable.ic_wifi_0,
                                R.drawable.ic_wifi_1,
                                R.drawable.ic_wifi_2,
                                R.drawable.ic_wifi_3,
                                R.drawable.ic_wifi_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_triangle),
                                R.drawable.ic_wifi_triangle_0,
                                R.drawable.ic_wifi_triangle_1,
                                R.drawable.ic_wifi_triangle_2,
                                R.drawable.ic_wifi_triangle_3,
                                R.drawable.ic_wifi_triangle_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_retro),
                                R.drawable.ic_wifi_retro_0,
                                R.drawable.ic_wifi_retro_1,
                                R.drawable.ic_wifi_retro_2,
                                R.drawable.ic_wifi_retro_3,
                                R.drawable.ic_wifi_retro_4
                        )
                )
        );

        return styles;
    }

    public class WifiReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (wifiManager.getWifiState() == WifiManager.WIFI_STATE_ENABLED) {
                int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4);
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(level), getContext().getTheme()));
            } else onDrawableUpdate(null);
        }
    }
}
