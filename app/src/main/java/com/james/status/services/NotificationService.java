package com.james.status.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationService extends NotificationListenerService {

    public static final String
            ACTION_CLEAR_ALL = "com.james.status.ACTION_CLEAR_ALL",
            ACTION_CLEAR = "com.james.status.ACTION_CLEAR",
            EXTRA_KEY = "com.james.status.EXTRA_INDEX",
            EXTRA_COMMAND = "com.james.status.EXTRA_COMMAND";

    private NotificationReceiver notificationReceiver;

    @Override
    public void onCreate() {
        super.onCreate();
        notificationReceiver = new NotificationReceiver();
        registerReceiver(notificationReceiver, new IntentFilter());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        unregisterReceiver(notificationReceiver);
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) return;

        ArrayList<StatusBarNotification> activeNotifications = new ArrayList<>();
        Collections.addAll(activeNotifications, getActiveNotifications());

        Intent intent = new Intent(StatusService.ACTION_NOTIFICATION);
        intent.setClass(this, StatusService.class);
        intent.putParcelableArrayListExtra(StatusService.EXTRA_NOTIFICATIONS, activeNotifications);

        startService(intent);
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled == null || !enabled) return;

        ArrayList<StatusBarNotification> activeNotifications = new ArrayList<>();
        Collections.addAll(activeNotifications, getActiveNotifications());

        Intent intent = new Intent(StatusService.ACTION_NOTIFICATION);
        intent.setClass(this, StatusService.class);
        intent.putParcelableArrayListExtra(StatusService.EXTRA_NOTIFICATIONS, activeNotifications);
        startService(intent);
    }

    private class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            switch (intent.getStringExtra(EXTRA_COMMAND)) {
                case ACTION_CLEAR_ALL:
                    cancelAllNotifications();
                    break;
                case ACTION_CLEAR:
                    cancelNotification(intent.getStringExtra(EXTRA_KEY));
                    break;
            }
        }
    }
}
