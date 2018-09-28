package com.james.status.data.icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.app.NotificationCompat;
import android.support.v4.util.ArrayMap;

import com.james.status.R;
import com.james.status.data.NotificationData;
import com.james.status.data.PreferenceData;
import com.james.status.data.preference.AppNotificationsPreferenceData;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;

import java.util.List;

public class NotificationsIconData extends IconData {

    private ArrayMap<String, NotificationData> notifications;

    public NotificationsIconData(Context context) {
        super(context);
        notifications = new ArrayMap<>();
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
    public boolean hasIcon() {
        return true;
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

        preferences.add(new AppNotificationsPreferenceData(
                getContext(),
                new BasePreferenceData.Identifier<String>(
                        PreferenceData.APP_NOTIFICATIONS,
                        "Blocked Apps",
                        getIdentifierArgs()
                )
        ));

        preferences.add(new IntegerPreferenceData(
                getContext(),
                new BasePreferenceData.Identifier<Integer>(
                        PreferenceData.APP_NOTIFICATIONS_MIN_PRIORITY,
                        "Minimum Priority Level",
                        getIdentifierArgs()
                ),
                null,
                NotificationCompat.PRIORITY_MIN,
                NotificationCompat.PRIORITY_MAX,
                null
        ));

        preferences.add(new BooleanPreferenceData(
                getContext(),
                new BasePreferenceData.Identifier<Boolean>(
                        PreferenceData.APP_NOTIFICATIONS_IGNORE_ONGOING,
                        "Ignore Persistent Notifications",
                        getIdentifierArgs()
                ),
                null
        ));

        return preferences;
    }

    @Override
    public void register() {
        super.register();
        notifications.clear();
        requestReDraw();
    }

    @Override
    public boolean needsDraw() {
        boolean needsDraw = false;
        for (int i = 0; i < notifications.size(); i++)
            needsDraw = needsDraw || !notifications.valueAt(i).getScale().isTarget();

        return needsDraw || super.needsDraw();
    }

    @Override
    public void draw(Canvas canvas, int x, int width) {
        updateAnimatedValues();
        int itemsWidth = notifications.size() * (iconSize.val() + padding.val());
        int items = itemsWidth <= width ? notifications.size() : (width / (iconSize.val() + padding.val())) - 1;

        x += padding.val();

        for (int i = 0; i < items && i < notifications.size(); i++) {
            NotificationData notification = notifications.valueAt(i);
            if (notification == null)
                continue;

            notification.getScale().next(isAnimations);

            Bitmap bitmap = notifications.valueAt(i).getIcon(getContext());
            if (bitmap != null) {
                float scaledIconSize = iconSize.val() * notification.getScale().val();
                iconPaint.setAlpha((int) (notification.getScale().val() * 255));

                if (iconSize.isTarget() && iconSize.val() != bitmap.getHeight() && notification.getScale().isTarget() && notification.getScale().val() == 1f)
                    bitmap = notification.getIcon(iconSize.val());

                Matrix matrix = new Matrix();
                matrix.postScale(scaledIconSize / bitmap.getHeight(), scaledIconSize / bitmap.getHeight());
                matrix.postTranslate(x + ((scaledIconSize - bitmap.getWidth()) / 2), ((float) canvas.getHeight() - scaledIconSize) / 2);
                canvas.drawBitmap(bitmap, matrix, iconPaint);

                x += scaledIconSize + padding.val();
            }
        }

        if (itemsWidth > width) {
            //TODO: draw "more" icon at x
        }
    }

    @Override
    public int getWidth(int height, int available) {
        if (available == -1)
            return (notifications.size() * (iconSize.val() + padding.val())) + padding.val();
        else if (available < (padding.val() * 2) + iconSize.val())
            return -1;
        else if (iconSize.val() == 0 && padding.val() == 0)
            return 0;
        else
            return (Math.min(notifications.size(), (available - padding.val()) / (iconSize.val() + padding.val()))
                    * (iconSize.val() + padding.val())) + padding.val();
    }

    private void addNotification(String key, NotificationData notification) {
        if (notification.priority < (int) PreferenceData.APP_NOTIFICATIONS_MIN_PRIORITY.getValue(getContext()))
            return;
        if (notification.isOngoing() && (boolean) PreferenceData.APP_NOTIFICATIONS_IGNORE_ONGOING.getValue(getContext()))
            return;

        for (int i = 0; i < notifications.size(); i++) {
            NotificationData notification2 = notifications.valueAt(i);
            if (notification2 != null && (notification.getKey().equals(notification2.getKey()) || notification.equals(notification2) || (notification.group != null && notification2.group != null && notification.group.equals(notification2.group)))) {
                if (notification2.set(notification))
                    requestReDraw();

                return;
            }
        }

        if (notification.getIcon(getContext()) != null) {
            notifications.put(key, notification);
            requestReDraw();
        }
    }

    private void removeNotification(String key) {
        notifications.remove(key);
        requestReDraw();
    }

    @Override
    public void onMessage(Object... message) {
        if (message.length > 0 && message[0] instanceof String) {
            if (message.length > 1 && message[1] instanceof NotificationData)
                addNotification((String) message[0], (NotificationData) message[1]);
            else removeNotification((String) message[0]);
        }
    }
}
