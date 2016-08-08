package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.utils.PreferenceUtils;

public class AirplaneModeIconData extends IconData<AirplaneModeIconData.AirplaneModeReceiver> {

    public AirplaneModeIconData(Context context, PreferenceUtils.PreferenceIdentifier identifier, DrawableListener drawableListener) {
        super(context, identifier, drawableListener);
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
    public IconStyleData getDefaultIconStyle() {
        return new IconStyleData(
                getContext().getString(R.string.icon_style_default),
                R.drawable.ic_airplane
        );
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
