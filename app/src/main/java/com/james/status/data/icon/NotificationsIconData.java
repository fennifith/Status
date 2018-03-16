package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.os.Build;
import android.support.v4.util.ArrayMap;

import com.james.status.R;
import com.james.status.data.NotificationData;
import com.james.status.data.PreferenceData;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.receivers.IconUpdateReceiver;
import com.james.status.services.NotificationService;
import com.james.status.utils.StaticUtils;

import java.util.List;

public class NotificationsIconData extends IconData<NotificationsIconData.NotificationReceiver> {

    public static final String ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED";
    public static final String ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED";
    public static final String EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION";

    private ArrayMap<String, NotificationData> notifications;

    public NotificationsIconData(Context context) {
        super(context);
        notifications = new ArrayMap<>();
    }

    @Override
    public NotificationReceiver getReceiver() {
        return new NotificationReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter filter = new IntentFilter();
        filter.addAction(ACTION_NOTIFICATION_ADDED);
        filter.addAction(ACTION_NOTIFICATION_REMOVED);
        return filter;
    }

    @Override
    public int getIconLayout() {
        return R.layout.layout_icon_notifications;
    }

    @Override
    public boolean canHazIcon() {
        return false;
    }

    @Override
    public boolean canHazText() {
        return false;
    }

    @Override
    public int getDefaultGravity() {
        return LEFT_GRAVITY;
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_notifications);
    }

    @Override
    public List<BasePreferenceData> getPreferences() {
        List<BasePreferenceData> preferences = super.getPreferences();

        preferences.add(new IntegerPreferenceData(
                getContext(),
                new BasePreferenceData.Identifier<Integer>(
                        PreferenceData.ICON_ICON_SCALE,
                        getContext().getString(R.string.preference_icon_scale),
                        getIdentifierArgs()
                ),
                getContext().getString(R.string.unit_dp),
                0,
                null,
                new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                    @Override
                    public void onPreferenceChange(Integer preference) {
                        StaticUtils.updateStatusService(getContext());
                    }
                }
        ));

        return preferences;
    }

    @Override
    public void register() {
        super.register();
        notifications.clear();
        requestReDraw();

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
            intent.setClass(getContext(), NotificationService.class);
            getContext().startService(intent);
        }
    }

    private void addNotification(NotificationData notification) {
        for (int i = 0; i < notifications.size(); i++) {
            NotificationData notification2 = notifications.valueAt(i);
            if (notification2 != null && (notification.getKey().equals(notification2.getKey()) || notification.equals(notification2) || (notification.group != null && notification2.group != null && notification.group.equals(notification2.group)))) {
                notifications.remove(notification2);
            }
        }

        if (notification.getIcon(getContext()) != null) {
            notifications.put(notification.getKey(), notification);
            requestReDraw();
        }
    }

    @Override
    public void draw(Canvas canvas, int x, int width) {
        updateAnimatedValues();
        int itemsWidth = notifications.size() * (drawnIconSize + drawnPadding);
        int items = itemsWidth <= width ? notifications.size() : (width / (drawnIconSize + drawnPadding)) - 1;

        x += drawnPadding;

        for (int i = 0; i < items; i++) {
            Bitmap bitmap = notifications.valueAt(i).getIcon(getContext());
            if (bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postScale(drawnIconSize / bitmap.getWidth(), drawnIconSize / bitmap.getWidth());
                matrix.postTranslate(x, canvas.getHeight() - drawnIconSize);
                canvas.drawBitmap(bitmap, matrix, iconPaint);

                x += drawnIconSize + drawnPadding;
            }
        }

        if (itemsWidth > width) {
            //TODO: draw "more" icon at x
        }
    }

    @Override
    public int getWidth(int height, int available) {
        if (available < 0)
            return -1;
        else if (drawnIconSize == 0 && drawnPadding == 0)
            return 0;
        else
            return Math.min(notifications.size(), available / (drawnIconSize + drawnPadding)) * (drawnIconSize + drawnPadding) + drawnPadding;
    }

    private void removeNotification(NotificationData notification) {
        notifications.remove(notification.getKey());
        requestReDraw();
    }

    static class NotificationReceiver extends IconUpdateReceiver<NotificationsIconData> {

        boolean isIconOverlapPrevention;

        private NotificationReceiver(NotificationsIconData iconData) {
            super(iconData);
            this.isIconOverlapPrevention = PreferenceData.STATUS_PREVENT_ICON_OVERLAP.getValue(iconData.getContext());
        }

        @Override
        public void onReceive(NotificationsIconData icon, Intent intent) {
            if (intent == null) return;
            String action = intent.getAction();
            if (action == null) return;

            NotificationData notification;
            if (intent.hasExtra(EXTRA_NOTIFICATION))
                notification = intent.getParcelableExtra(EXTRA_NOTIFICATION);
            else return;

            switch (action) {
                case ACTION_NOTIFICATION_ADDED:
                    icon.addNotification(notification);
                    break;
                case ACTION_NOTIFICATION_REMOVED:
                    icon.removeNotification(notification);
                    break;
            }
        }
    }
}
