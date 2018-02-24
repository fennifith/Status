package com.james.status.data.icon;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class AirplaneModeIconData extends IconData<AirplaneModeIconData.AirplaneModeReceiver> {

    public AirplaneModeIconData(Context context) {
        super(context);
    }

    @Override
    public AirplaneModeReceiver getReceiver() {
        return new AirplaneModeReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_airplane);
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    @Override
    public int getIconStyleSize() {
        return 1;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_airplane
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_front),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_airplane_front"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_propeller),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_airplane_propeller"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_pilot_hat),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_airplane_pilot_hat"
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_balloon),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_airplane_air_balloon
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_airplane)
        };
    }

    static class AirplaneModeReceiver extends IconUpdateReceiver<AirplaneModeIconData> {

        private AirplaneModeReceiver(AirplaneModeIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(AirplaneModeIconData icon, Intent intent) {
            if (intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false))
                icon.onIconUpdate(0);
            else icon.onIconUpdate(-1);
        }
    }
}
