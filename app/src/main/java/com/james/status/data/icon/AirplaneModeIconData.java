package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class AirplaneModeIconData extends IconData<AirplaneModeIconData.AirplaneModeReceiver> {

    public AirplaneModeIconData(Context context) {
        super(context);
    }

    @Override
    public AirplaneModeReceiver getReceiver() {
        return new AirplaneModeReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(Intent.ACTION_AIRPLANE_MODE_CHANGED);
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{R.drawable.ic_airplane};
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_airplane);
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = new ArrayList<>();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                R.drawable.ic_airplane
                        )
                )
        );

        return styles;
    }

    public class AirplaneModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false))
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
            else onDrawableUpdate(null);
        }
    }
}
