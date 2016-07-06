package com.james.status.services;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

public class NotificationService extends NotificationListenerService {

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
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
    }

    class NotificationReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.getStringExtra("command").equals("clearall")) {
                cancelAllNotifications();
            } else if(intent.getStringExtra("command").equals("list")){
                getActiveNotifications();
            }
        }
    }
}
