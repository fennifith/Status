package com.james.status.data.icon;

import android.content.Context;
import android.os.Build;
import android.support.graphics.drawable.VectorDrawableCompat;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.SubscriptionInfo;
import android.telephony.SubscriptionManager;
import android.telephony.TelephonyManager;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

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
                        for (SubscriptionInfo info : subscriptionManager.getActiveSubscriptionInfoList()) {
                            if (info.getSimSlotIndex() == 1) {
                                networkListener = new NetworkListener(info.getSubscriptionId());
                                break;
                            }
                        }
                    }
                }

                if (networkListener != null)
                    telephonyManager.listen(networkListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS);
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

        public NetworkListener(int id) {
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
                    if (evdoLevel == 0) {
                        level = cdmaLevel;
                    } else if (cdmaLevel == 0) {
                        level = evdoLevel;
                    } else {
                        level = cdmaLevel < evdoLevel ? cdmaLevel : evdoLevel;
                    }
                }

                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(level), getContext().getTheme()));
            }
        }
    }
}
