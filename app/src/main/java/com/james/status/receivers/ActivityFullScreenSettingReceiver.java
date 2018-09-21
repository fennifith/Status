package com.james.status.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

public class ActivityFullScreenSettingReceiver extends BroadcastReceiver {

    public static final String EXTRA_COMPONENT = "com.james.status.EXTRA_COMPONENT";
    public static final String EXTRA_FULLSCREEN = "com.james.status.EXTRA_FULLSCREEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_COMPONENT) && intent.hasExtra(EXTRA_FULLSCREEN)) {
            PreferenceData.APP_FULLSCREEN.setValue(context, !intent.getBooleanExtra(EXTRA_FULLSCREEN, true), intent.getStringExtra(EXTRA_COMPONENT));
            StaticUtils.updateStatusService(context, true);
        }
    }
}
