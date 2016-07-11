package com.james.status.services;

import android.content.Intent;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.Collections;

public class NotificationService extends NotificationListenerService {

    public static final String ACTION_GET_NOTIFICATIONS = "com.james.status.ACTION_GET_NOTIFICATIONS";

    private boolean isConnected, shouldSendOnConnect;

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (action == null) return START_STICKY;
        switch (action) {
            case ACTION_GET_NOTIFICATIONS:
                if (isConnected) sendNotifications();
                else shouldSendOnConnect = true;
                break;
        }

        return super.onStartCommand(intent, flags, startId);
    }

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        isConnected = true;

        if (shouldSendOnConnect) {
            sendNotifications();
            shouldSendOnConnect = false;
        }
    }

    @Override
    public void onListenerDisconnected() {
        super.onListenerDisconnected();
        isConnected = false;
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_ADDED);
            intent.setClass(this, StatusService.class);
            intent.putExtra(StatusService.EXTRA_NOTIFICATION, sbn);
            startService(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_REMOVED);
            intent.setClass(this, StatusService.class);
            intent.putExtra(StatusService.EXTRA_NOTIFICATION, sbn);
            startService(intent);
        }
    }

    private ArrayList<StatusBarNotification> getNotifications() {
        ArrayList<StatusBarNotification> activeNotifications = new ArrayList<>();
        StatusBarNotification[] notifications = getActiveNotifications();
        if (notifications != null) Collections.addAll(activeNotifications, notifications);
        return activeNotifications;
    }

    private void sendNotifications() {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION);
            intent.setClass(this, StatusService.class);
            intent.putParcelableArrayListExtra(StatusService.EXTRA_NOTIFICATIONS, getNotifications());
            startService(intent);
        }
    }
}
