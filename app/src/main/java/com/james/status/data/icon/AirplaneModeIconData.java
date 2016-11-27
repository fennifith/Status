package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
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
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_balloon),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_airplane_air_balloon
                        )
                )
        );

        return styles;
    }

    static class AirplaneModeReceiver extends IconUpdateReceiver<AirplaneModeIconData> {

        private AirplaneModeReceiver(AirplaneModeIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(AirplaneModeIconData icon, Intent intent) {
            if (intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false))
                icon.onDrawableUpdate(0);
            else icon.onDrawableUpdate(-1);
        }
    }
}
