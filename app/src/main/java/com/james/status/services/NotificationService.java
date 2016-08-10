package com.james.status.services;

import android.annotation.TargetApi;
import android.app.Notification;
import android.content.Intent;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;

@TargetApi(18)
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

            intent.putExtra(StatusService.EXTRA_NOTIFICATION, sbn.getNotification());
            intent.putExtra(StatusService.EXTRA_PACKAGE_NAME, sbn.getPackageName());

            startService(intent);
        }
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled) {
            Intent intent = new Intent(StatusService.ACTION_NOTIFICATION_REMOVED);
            intent.setClass(this, StatusService.class);

            intent.putExtra(StatusService.EXTRA_NOTIFICATION, sbn.getNotification());
            intent.putExtra(StatusService.EXTRA_PACKAGE_NAME, sbn.getPackageName());

            startService(intent);
        }
    }

    private ArrayList<Notification> getNotifications() {
        ArrayList<Notification> activeNotifications = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            StatusBarNotification[] notifications = getActiveNotifications();
            for (StatusBarNotification notification : notifications) {
                activeNotifications.add(notification.getNotification());
            }
        }
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
