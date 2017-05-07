package com.james.status.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.james.status.data.AppData;
import com.james.status.utils.StaticUtils;

public class ActivityVisibilitySettingReceiver extends BroadcastReceiver {

    public static final String EXTRA_ACTIVITY = "com.james.status.EXTRA_ACTIVITY";
    public static final String EXTRA_VISIBILITY = "com.james.status.EXTRA_VISIBILITY";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_ACTIVITY) && intent.hasExtra(EXTRA_VISIBILITY)) {
            AppData.ActivityData activity = intent.getParcelableExtra(EXTRA_ACTIVITY);
            activity.putPreference(context, AppData.PreferenceIdentifier.FULLSCREEN, !intent.getBooleanExtra(EXTRA_VISIBILITY, true));
            StaticUtils.updateStatusService(context);
        }
    }
}
