package com.james.status.data.icon;

import android.content.Context;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

import java.lang.reflect.Field;

public class DualNetworkIconData extends IconData {

    private TelephonyManager telephonyManager;
    private SubscriptionManager subscriptionManager;
    private NetworkListener networkListener;
    private boolean isRegistered;

    public DualNetworkIconData(Context context) {
        super(context, PreferenceUtils.PreferenceIdentifier.STYLE_NETWORK_ICON);

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1)
            subscriptionManager = (SubscriptionManager) context.getSystemService(Context.TELEPHONY_SUBSCRIPTION_SERVICE);
    }

    @Override
    public void register() {
        if (telephonyManager != null) {
            try {
                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP_MR1 && subscriptionManager != null) {

                    if (networkListener == null) {
                        int index = 0;
                        for (SubscriptionInfo info : subscriptionManager.getActiveSubscriptionInfoList()) {
                            if (info.getSimSlotIndex() > index) {
                                networkListener = new NetworkListener(info.getSubscriptionId());
                                index = info.getSimSlotIndex();
                            }
                        }
                    }
                }

                if (networkListener != null)
                    telephonyManager.listen(networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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

        public NetworkListener(int id) {
            super();
            try {
                Field field = this.getClass().getSuperclass().getDeclaredField("mSubId");
                field.setAccessible(true);
                field.set(this, id);
            } catch (Exception e) {
                e.printStackTrace();
            }
        }

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);

            if (isRegistered) {
                int level = 0;

                int strength = signalStrength.getGsmSignalStrength();
                if (strength != 99 && strength != 0) level = (int) (strength / 7.75);
                else {
                    strength = signalStrength.getCdmaDbm();

                    if (strength < -100) level = 0;
                    else if (strength < -95) level = 1;
                    else if (strength < -85) level = 2;
                    else if (strength < -75) level = 3;
                    else if (strength != 0) level = 4;
                    else {
                        strength = signalStrength.getEvdoDbm();

                        if (strength == 0 || strength < -100) level = 0;
                        else if (strength < -95) level = 1;
                        else if (strength < -85) level = 2;
                        else if (strength < -75) level = 3;
                        else level = 4;
                    }
                }

                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(level), getContext().getTheme()));
            }
        }
    }
}
