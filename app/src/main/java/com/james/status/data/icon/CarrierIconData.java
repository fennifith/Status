package com.james.status.data.icon;

import android.Manifest;
import android.content.Context;
import android.telephony.TelephonyManager;

import com.james.status.R;

public class CarrierIconData extends IconData {

    private TelephonyManager telephonyManager;

    public CarrierIconData(Context context) {
        super(context);
        telephonyManager = (TelephonyManager) context.getSystemService(Context.TELEPHONY_SERVICE);
    }

    @Override
    boolean isDefaultVisible() {
        return false;
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.READ_PHONE_STATE};
    }

    @Override
    public boolean canHazDrawable() {
        return false;
    }

    @Override
    public boolean hasDrawable() {
        return false;
    }

    @Override
    public boolean canHazText() {
        return true;
    }

    @Override
    public boolean hasText() {
        return true;
    }

    @Override
    public void register() {
        onTextUpdate(telephonyManager.getNetworkOperatorName());
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_carrier);
    }
}
