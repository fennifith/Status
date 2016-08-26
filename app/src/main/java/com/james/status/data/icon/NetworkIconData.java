package com.james.status.data.icon;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class NetworkIconData extends IconData {

    private TelephonyManager telephonyManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public NetworkIconData(Context context) {
        super(context, PreferenceUtils.PreferenceIdentifier.STYLE_NETWORK_ICON);
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
    public int[] getDefaultIconResource() {
        return new int[]{
                R.drawable.ic_signal_0,
                R.drawable.ic_signal_1,
                R.drawable.ic_signal_2,
                R.drawable.ic_signal_3,
                R.drawable.ic_signal_4
        };
    }

    private class NetworkListener extends PhoneStateListener {
        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (isRegistered) {
                int level;

                if (signalStrength.isGsm()) {
                    int strength = signalStrength.getGsmSignalStrength();

                    if (strength <= 2 || strength == 99) level = 0;
                    else if (strength >= 12) level = 4;
                    else if (strength >= 8) level = 3;
                    else if (strength >= 5) level = 2;
                    else level = 1;
                } else {
                    int cdmaLevel = StaticUtils.getSignalStrength(signalStrength.getCdmaDbm(), signalStrength.getCdmaEcio());
                    int evdoLevel = StaticUtils.getSignalStrength(signalStrength.getEvdoDbm(), signalStrength.getEvdoEcio());
                    if (cdmaLevel != 0) level = cdmaLevel;
                    else level = evdoLevel;
                }

                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(level), getContext().getTheme()));
            }
        }
    }
}
