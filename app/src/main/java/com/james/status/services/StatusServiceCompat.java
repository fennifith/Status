package com.james.status.services;

import android.app.Service;
import android.content.Intent;
import android.os.Build;
import android.os.IBinder;
import android.support.annotation.Nullable;

import com.james.status.data.NotificationData;

public class StatusServiceCompat extends Service {

    public static final String ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED";
    public static final String ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED";
    public static final String EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION";

    private StatusServiceImpl impl;

    @Override
    public void onCreate() {
        super.onCreate();
        if (impl == null)
            impl = new StatusServiceImpl(this);

        impl.onCreate();
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        if (intent == null) return START_STICKY;
        String action = intent.getAction();
        if (action == null) return START_STICKY;

        if (action.equals(ACTION_NOTIFICATION_ADDED) && intent.hasExtra(EXTRA_NOTIFICATION)) {
            NotificationData notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
            impl.onNotificationAdded(notification.getKey(), notification);
        } else if (action.equals(ACTION_NOTIFICATION_REMOVED) && intent.hasExtra(EXTRA_NOTIFICATION)) {
            NotificationData notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
            impl.onNotificationRemoved(notification.getKey());
        } else return impl.onStartCommand(intent, flags, startId);

        return START_STICKY;
    }

    @Override
    public void onTaskRemoved(Intent rootIntent) {
        if (Build.VERSION.SDK_INT == Build.VERSION_CODES.KITKAT)
            impl.onTaskRemoved(rootIntent);
        else super.onTaskRemoved(rootIntent);
    }

    @Override
    public void onDestroy() {
        impl.onDestroy();
        super.onDestroy();
    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

}
