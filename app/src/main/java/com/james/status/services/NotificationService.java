package com.james.status.services;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationService extends NotificationListenerService {

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
}
