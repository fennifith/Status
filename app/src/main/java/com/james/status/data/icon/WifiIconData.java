package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.List;

public class WifiIconData extends IconData<WifiIconData.WifiReceiver> {

    private WifiManager wifiManager;
    private ConnectivityManager connectivityManager;

    public WifiIconData(Context context) {
        super(context);
        wifiManager = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        connectivityManager = (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
    }

    @Override
    public WifiReceiver getReceiver() {
        return new WifiReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(WifiManager.NETWORK_STATE_CHANGED_ACTION);
    }

    @Override
    public void register() {
        super.register();

        int level = WifiManager.calculateSignalLevel(wifiManager.getConnectionInfo().getRssi(), 4);
        if (level > 0) { //temporary fix, cannot determine wifi connection without BroadcastReceiver for some reason
            onDrawableUpdate(level);
        }
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_wifi);
    }

    @Override
    public int getIconStyleSize() {
        return 5;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_wifi_0,
                                R.drawable.ic_wifi_1,
                                R.drawable.ic_wifi_2,
                                R.drawable.ic_wifi_3,
                                R.drawable.ic_wifi_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_radial),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_wifi_radial_0,
                                R.drawable.ic_wifi_radial_1,
                                R.drawable.ic_wifi_radial_2,
                                R.drawable.ic_wifi_radial_3,
                                R.drawable.ic_wifi_radial_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_triangle),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_wifi_triangle_0,
                                R.drawable.ic_wifi_triangle_1,
                                R.drawable.ic_wifi_triangle_2,
                                R.drawable.ic_wifi_triangle_3,
                                R.drawable.ic_wifi_triangle_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_retro),
                                IconStyleData.TYPE_VECTOR,
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

    static class WifiReceiver extends IconUpdateReceiver<WifiIconData> {

        public WifiReceiver(WifiIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(WifiIconData icon, Intent intent) {
            NetworkInfo networkInfo = intent.getParcelableExtra(WifiManager.EXTRA_NETWORK_INFO);
            if (networkInfo == null) networkInfo = icon.connectivityManager.getActiveNetworkInfo();

            if (networkInfo.getType() == ConnectivityManager.TYPE_WIFI && networkInfo.isConnected())
                icon.onDrawableUpdate(WifiManager.calculateSignalLevel(icon.wifiManager.getConnectionInfo().getRssi(), 4));
            else icon.onDrawableUpdate(-1);
        }
    }
}
