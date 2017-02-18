package com.james.status.data.icon;

import android.animation.LayoutTransition;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.v4.util.ArrayMap;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.james.status.R;
import com.james.status.data.NotificationData;
import com.james.status.data.preference.IntegerPreferenceData;
import com.james.status.data.preference.PreferenceData;
import com.james.status.receivers.IconUpdateReceiver;
import com.james.status.services.NotificationService;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;

import java.util.List;

public class NotificationsIconData extends IconData<NotificationsIconData.NotificationReceiver> {

    public static final String ACTION_NOTIFICATION_ADDED = "com.james.status.ACTION_NOTIFICATION_ADDED";
    public static final String ACTION_NOTIFICATION_REMOVED = "com.james.status.ACTION_NOTIFICATION_REMOVED";
    public static final String EXTRA_NOTIFICATION = "com.james.status.EXTRA_NOTIFICATION";

    private LayoutInflater inflater;

    private LinearLayout notificationLayout;
    private ArrayMap<String, NotificationData> notifications;

    public NotificationsIconData(Context context) {
        super(context);
        notifications = new ArrayMap<>();
        inflater = LayoutInflater.from(context);
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
    public boolean canHazDrawable() {
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
    public List<PreferenceData> getPreferences() {
        List<PreferenceData> preferences = super.getPreferences();

        preferences.add(new IntegerPreferenceData(
                getContext(),
                new PreferenceData.Identifier(
                        getContext().getString(R.string.preference_icon_scale)
                ),
                getIconScale(),
                getContext().getString(R.string.unit_dp),
                0,
                null,
                new PreferenceData.OnPreferenceChangeListener<Integer>() {
                    @Override
                    public void onPreferenceChange(Integer preference) {
                        putPreference(PreferenceIdentifier.ICON_SCALE, preference);
                        StaticUtils.updateStatusService(getContext());
                    }
                }
        ));

        return preferences;
    }

    @Override
    public void register() {
        super.register();
        notificationLayout = (LinearLayout) getIconView();
        notificationLayout.setPadding(getIconPadding(), 0, getIconPadding(), 0);

        notificationLayout.removeAllViewsInLayout();
        notifications.clear();

        Boolean isIconAnimations = PreferenceUtils.getBooleanPreference(getContext(), PreferenceUtils.PreferenceIdentifier.STATUS_ICON_ANIMATIONS);
        isIconAnimations = isIconAnimations != null ? isIconAnimations : true;

        notificationLayout.setLayoutTransition(isIconAnimations ? new LayoutTransition() : null);

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2) {
            Intent intent = new Intent(NotificationService.ACTION_GET_NOTIFICATIONS);
            intent.setClass(getContext(), NotificationService.class);
            getContext().startService(intent);
        }
    }

    @Override
    public void unregister() {
        super.unregister();
        notificationLayout = null;
    }

    private void addNotification(NotificationData notification) {
        if (notificationLayout != null) {
            View v = null;

            for (int i = 0; i < notificationLayout.getChildCount(); i++) {
                View child = notificationLayout.getChildAt(i);
                Object tag = child.getTag();

                if (tag != null && tag instanceof String) {
                    NotificationData notification2 = notifications.get(tag);
                    if (notification2 != null && (notification.getKey().equals(notification2.getKey()) || notification.equals(notification2) || (notification.group != null && notification2.group != null && notification.group.equals(notification2.group)))) {
                        if (v != null && v.getParent() != null) {
                            notificationLayout.removeView(child);
                            i--;

                            Log.e("Notification", "removing view for " + notification2.getKey() + " in group " + notification2.group);
                        } else
                            Log.e("Notification", "removing notification " + notification2.getKey() + " in group " + notification2.group);
                        v = child;
                        notifications.remove(notification2);
                    }
                }
            }

            if (v == null) {
                v = inflater.inflate(R.layout.item_icon, notificationLayout, false);
                v.setTag(notification.getKey());
            }

            v.setPadding(getIconPadding(), 0, getIconPadding(), 0);
            v.findViewById(R.id.text).setVisibility(View.GONE);

            Drawable drawable = notification.getIcon(getContext());

            if (drawable != null) {
                CustomImageView iconView = (CustomImageView) v.findViewById(R.id.icon);
                iconView.setImageDrawable(drawable, getColor());

                ViewGroup.LayoutParams layoutParams = iconView.getLayoutParams();
                if (layoutParams != null)
                    layoutParams.height = (int) StaticUtils.getPixelsFromDp(getIconScale());
                else
                    layoutParams = new ViewGroup.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, (int) StaticUtils.getPixelsFromDp(getIconScale()));

                iconView.setLayoutParams(layoutParams);

                if (v.getParent() == null) notificationLayout.addView(v, 0);
                notifications.put(notification.getKey(), notification);

                onDrawableUpdate(-1);
                notificationLayout.setVisibility(View.VISIBLE);
            }
        }
    }

    private void removeNotification(NotificationData notification) {
        if (notificationLayout != null) {
            for (int i = 0; i < notificationLayout.getChildCount(); i++) {
                View child = notificationLayout.getChildAt(i);
                if (((String) child.getTag()).matches(notification.getKey()) && notifications.containsKey(notification.getKey())) {
                    notificationLayout.removeViewAt(i);
                    notifications.remove(notification.getKey());
                }
            }
        }

        if (notifications.size() < 1) notificationLayout.setVisibility(View.GONE);
    }

    static class NotificationReceiver extends IconUpdateReceiver<NotificationsIconData> {

        private NotificationReceiver(NotificationsIconData iconData) {
            super(iconData);
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
