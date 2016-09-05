package com.james.status.data.icon;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.TelephonyManager;

import com.james.status.R;

//HAH IT'S A ICON DATA FOR THE DATA ICON GET IT BECAUSE THEIR NAMES ARE THE SAME BUT REVERSED YEAH IT'S SO FUNNY HAHHAHAHAHAHAHAH I KNOW RIGHT IT'S REALLY HILARIOUS I'M LITERALLY DYING OF LAUGHTER
public class DataIconData extends IconData {

    private TelephonyManager telephonyManager;
    private DataListener dataListener;

    private boolean isRegistered;

    public DataIconData(Context context) {
        super(context);

        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
        dataListener = new DataListener();
    }

    @Override
    public boolean hasDrawable() {
        return false;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public String getFakeText() {
        return "4G";
    }

    @Override
    public void register() {
        if (dataListener != null)
            telephonyManager.listen(dataListener, PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
        isRegistered = true;

        if (telephonyManager.getDataState() != TelephonyManager.DATA_DISCONNECTED) {
            switch (telephonyManager.getNetworkType()) {
                case TelephonyManager.NETWORK_TYPE_GPRS:
                case 16:
                case TelephonyManager.NETWORK_TYPE_EDGE:
                case TelephonyManager.NETWORK_TYPE_CDMA:
                case TelephonyManager.NETWORK_TYPE_1xRTT:
                case TelephonyManager.NETWORK_TYPE_IDEN:
                    onTextUpdate("2G");
                    break;
                case TelephonyManager.NETWORK_TYPE_UMTS:
                case TelephonyManager.NETWORK_TYPE_EVDO_0:
                case TelephonyManager.NETWORK_TYPE_EVDO_A:
                case TelephonyManager.NETWORK_TYPE_HSDPA:
                case TelephonyManager.NETWORK_TYPE_HSUPA:
                case TelephonyManager.NETWORK_TYPE_HSPA:
                case TelephonyManager.NETWORK_TYPE_EVDO_B:
                case TelephonyManager.NETWORK_TYPE_EHRPD:
                case TelephonyManager.NETWORK_TYPE_HSPAP:
                case 17:
                    onTextUpdate("3G");
                    break;
                case TelephonyManager.NETWORK_TYPE_LTE:
                case 18:
                    onTextUpdate("4G");
                    break;
                default:
                    onTextUpdate(null);
                    break;
            }
        } else onTextUpdate(null);
    }

    @Override
    public void unregister() {
        isRegistered = false;
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_data);
    }

    private class DataListener extends PhoneStateListener {
        @Override
        public void onDataConnectionStateChanged(int state, int networkType) {
            super.onDataConnectionStateChanged(state, networkType);

            if (isRegistered) {
                if (state == TelephonyManager.DATA_DISCONNECTED) onTextUpdate(null);
                else {
                    switch (telephonyManager.getNetworkType()) {
                        case TelephonyManager.NETWORK_TYPE_GPRS:
                        case 16:
                        case TelephonyManager.NETWORK_TYPE_EDGE:
                        case TelephonyManager.NETWORK_TYPE_CDMA:
                        case TelephonyManager.NETWORK_TYPE_1xRTT:
                        case TelephonyManager.NETWORK_TYPE_IDEN:
                            onTextUpdate("2G");
                            break;
                        case TelephonyManager.NETWORK_TYPE_UMTS:
                        case TelephonyManager.NETWORK_TYPE_EVDO_0:
                        case TelephonyManager.NETWORK_TYPE_EVDO_A:
                        case TelephonyManager.NETWORK_TYPE_HSDPA:
                        case TelephonyManager.NETWORK_TYPE_HSUPA:
                        case TelephonyManager.NETWORK_TYPE_HSPA:
                        case TelephonyManager.NETWORK_TYPE_EVDO_B:
                        case TelephonyManager.NETWORK_TYPE_EHRPD:
                        case TelephonyManager.NETWORK_TYPE_HSPAP:
                        case 17:
                            onTextUpdate("3G");
                            break;
                        case TelephonyManager.NETWORK_TYPE_LTE:
                        case 18:
                            onTextUpdate("4G");
                            break;
                        default:
                            onTextUpdate(null);
                            break;
                    }
                }
            }
        }
    }
}
