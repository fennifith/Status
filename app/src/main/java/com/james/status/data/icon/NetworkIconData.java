package com.james.status.data.icon;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.Arrays;
import java.util.List;

public class NetworkIconData extends IconData {

    private TelephonyManager telephonyManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public NetworkIconData(Context context) {
        super(context);
        telephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void register() {
        if (networkListener == null) {
            networkListener = new NetworkListener();
            telephonyManager.listen(networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
        }
        isRegistered = true;
    }

    @Override
    public void unregister() {
        isRegistered = false;
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_network);
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
                                R.drawable.ic_signal_0,
                                R.drawable.ic_signal_1,
                                R.drawable.ic_signal_2,
                                R.drawable.ic_signal_3,
                                R.drawable.ic_signal_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_square),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_signal_square_0,
                                R.drawable.ic_signal_square_1,
                                R.drawable.ic_signal_square_2,
                                R.drawable.ic_signal_square_3,
                                R.drawable.ic_signal_square_4
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_retro),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_signal_retro_0,
                                R.drawable.ic_signal_retro_1,
                                R.drawable.ic_signal_retro_2,
                                R.drawable.ic_signal_retro_3,
                                R.drawable.ic_signal_retro_4
                        )
                )
        );

        return styles;
    }

    private class NetworkListener extends PhoneStateListener {

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (isRegistered) {
                int level;

                if (signalStrength.isGsm()) {
                    if (signalStrength.getGsmSignalStrength() != 99)
                        level = signalStrength.getGsmSignalStrength() * 2 - 113;
                    else
                        level = signalStrength.getGsmSignalStrength();
                } else
                    level = signalStrength.getCdmaDbm();

                if (level < -100) level = 0;
                else if (level < -95) level = 1;
                else if (level < -85) level = 2;
                else if (level < -75) level = 3;
                else if (level != 0) level = 4;
                else level = -1;

                onDrawableUpdate(level);
            }
        }
    }
}
