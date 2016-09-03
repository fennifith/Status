package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.TelephonyManager;

import com.james.status.R;

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

    public class AirplaneModeReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getBooleanExtra(TelephonyManager.EXTRA_STATE, false))
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(), getContext().getTheme()));
            else onDrawableUpdate(null);
        }
    }
}
