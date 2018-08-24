package com.james.status.data.icon;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Matrix;
import android.support.v4.util.ArrayMap;

import com.james.status.R;
import com.james.status.data.NotificationData;
import com.james.status.data.PreferenceData;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.utils.StaticUtils;

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
                        StaticUtils.updateStatusService(getContext(), true);
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
    }

    @Override
    public void draw(Canvas canvas, int x, int width) {
        updateAnimatedValues();
        int itemsWidth = notifications.size() * (iconSize.val() + padding.val());
        int items = itemsWidth <= width ? notifications.size() : (width / (iconSize.val() + padding.val())) - 1;

        x += padding.val();

        for (int i = 0; i < items; i++) {
            Bitmap bitmap = notifications.valueAt(i).getIcon(getContext());
            if (bitmap != null) {
                Matrix matrix = new Matrix();
                matrix.postScale((float) iconSize.val() / bitmap.getHeight(), (float) iconSize.val() / bitmap.getHeight());
                matrix.postTranslate(x, ((float) canvas.getHeight() - iconSize.val()) / 2);
                canvas.drawBitmap(bitmap, matrix, iconPaint);

                x += iconSize.val() + padding.val();
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

    private void removeNotification(String key) {
        notifications.remove(key);
        requestReDraw();
    }

    @Override
    public void onMessage(Object... message) {
        if (message.length > 0 && message[0] instanceof String) {
            if (message.length > 1 && message[1] instanceof NotificationData)
                addNotification((NotificationData) message[1]);
            else removeNotification((String) message[0]);
        }
    }
}
