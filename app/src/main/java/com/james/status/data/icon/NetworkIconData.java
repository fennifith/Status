package com.james.status.data.icon;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.utils.PreferenceUtils;

public class NetworkIconData extends IconData {

    private TelephonyManager telephonyManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public NetworkIconData(Context context, PreferenceUtils.PreferenceIdentifier identifier, DrawableListener drawableListener) {
        super(context, identifier, drawableListener);
        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        networkListener = new NetworkListener();
    }

    @Override
    public void register() {
        if (networkListener != null)
            telephonyManager.listen(networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        isRegistered = true;
    }

    @Override
    public void unregister() {
        isRegistered = false;
    }

    @Override
    public IconStyleData getDefaultIconStyle() {
        return new IconStyleData(
                getContext().getString(R.string.icon_style_default),
                R.drawable.ic_signal_0,
                R.drawable.ic_signal_1,
                R.drawable.ic_signal_2,
                R.drawable.ic_signal_3,
                R.drawable.ic_signal_4
        );
    }

    private class NetworkListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (isRegistered)
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(signalStrength.getGsmSignalStrength()), getContext().getTheme()));
        }
    }
}
