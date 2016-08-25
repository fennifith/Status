package com.james.status.data.icon;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DualNetworkIconData extends IconData {

    private Object telephonyManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public DualNetworkIconData(Context context) {
        super(context, PreferenceUtils.PreferenceIdentifier.STYLE_NETWORK_ICON);

        try {
            final Class<?> tmClass = Class.forName("android.telephony.MultiSimTelephonyManager");
            Method methodDefault = tmClass.getDeclaredMethod("getDefault", int.class);
            methodDefault.setAccessible(true);

            telephonyManager = methodDefault.invoke(null, 1);
        } catch (Exception e) {
            e.printStackTrace();
        }

        networkListener = new NetworkListener();
    }

    @Override
    public void register() {
        if (networkListener != null && telephonyManager != null) {
            try {
                Method method = telephonyManager.getClass().getMethod("listen", void.class);
                method.invoke(telephonyManager, networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

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

        public NetworkListener() {
            super();
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("mSubscription");
                field.setAccessible(true);
                field.set(this, 1);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            if (isRegistered)
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(signalStrength.getGsmSignalStrength()), getContext().getTheme()));
        }
    }
}
