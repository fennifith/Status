package com.james.status.data.icon;

import android.content.Context;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

public class DualNetworkIconData extends IconData {

    private TelephonyManager telephonyManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public DualNetworkIconData(Context context) {
        super(context, PreferenceUtils.PreferenceIdentifier.STYLE_NETWORK_ICON);

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    public void register() {
        if (telephonyManager != null) {
            try {
                final Class<?> subscriptionManager = Class.forName("android.telephony.SubscriptionManager");

                Method getActiveSubIdList = subscriptionManager.getDeclaredMethod("getActiveSubIdList");
                long[] subIdList = (long[]) getActiveSubIdList.invoke(null);

                if (subIdList.length > 1 && networkListener == null) {
                    networkListener = new NetworkListener(subIdList[1]);
                }

                if (networkListener != null) {
                    telephonyManager.listen(networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
                }
            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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

        public NetworkListener(long id) {
            super();
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("mSubId");
                field.setAccessible(true);
                field.set(this, id);
            } catch (Exception e) {
                e.printStackTrace();

                Toast.makeText(getContext(), e.getMessage(), Toast.LENGTH_SHORT).show();
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
