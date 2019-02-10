/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.services;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.service.notification.NotificationListenerService;
import android.service.notification.StatusBarNotification;

import com.james.status.data.NotificationData;
import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.core.app.NotificationCompat;

@TargetApi(18)
public class StatusService extends NotificationListenerService {

    private PackageManager packageManager;
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

        return impl.onStartCommand(intent, flags, startId);
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

    @Override
    public void onListenerConnected() {
        super.onListenerConnected();
        packageManager = getPackageManager();
        sendNotifications();
    }

    @Override
    public void onNotificationPosted(StatusBarNotification sbn) {
        if ((boolean) PreferenceData.STATUS_ENABLED.getValue(this) && (boolean) PreferenceData.APP_NOTIFICATIONS.getSpecificValue(this, sbn.getPackageName()) && !StaticUtils.shouldUseCompatNotifications(this) && !sbn.getPackageName().matches("com.james.status"))
            impl.onNotificationAdded(getKey(sbn), new NotificationData(sbn, getKey(sbn)));
    }

    @Override
    public void onNotificationRemoved(StatusBarNotification sbn) {
        if ((boolean) PreferenceData.STATUS_ENABLED.getValue(this) && !StaticUtils.shouldUseCompatNotifications(this))
            impl.onNotificationRemoved(getKey(sbn));
    }

    private ArrayList<StatusBarNotification> getNotifications() {
        ArrayList<StatusBarNotification> activeNotifications = new ArrayList<>();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            try {
                activeNotifications.addAll(Arrays.asList(getActiveNotifications()));
            } catch (NullPointerException ignored) {
            }
        }
        return activeNotifications;
    }

    public void sendNotifications() {
        if (packageManager == null)
            packageManager = getPackageManager();
        if (packageManager == null)
            return;

        if ((boolean) PreferenceData.STATUS_ENABLED.getValue(this) && !StaticUtils.shouldUseCompatNotifications(this)) {
            List<StatusBarNotification> notifications = getNotifications();
            Collections.reverse(notifications);

            for (StatusBarNotification sbn : notifications) {
                if (sbn == null || sbn.getPackageName().matches("com.james.status"))
                    continue;

                if (!((boolean) PreferenceData.APP_NOTIFICATIONS.getSpecificValue(this, sbn.getPackageName())))
                    continue;

                NotificationData notification = new NotificationData(sbn, getKey(sbn));
                notification.priority = NotificationCompat.PRIORITY_DEFAULT;

                impl.onNotificationAdded(getKey(sbn), notification);
            }
        }
    }

    private String getKey(StatusBarNotification statusBarNotification) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return statusBarNotification.getKey();
        else
            return statusBarNotification.getPackageName() + "/" + String.valueOf(statusBarNotification.getId());
    }
}
