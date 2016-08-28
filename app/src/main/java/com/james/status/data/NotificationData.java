package com.james.status.data;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.app.NotificationCompat;

import com.james.status.utils.PreferenceUtils;

public class NotificationData implements Parcelable {

    public String category, title, subtitle, packageName;
    public int priority, id, icon;
    private Icon unloadedIcon;

    public PendingIntent intent;
    public ActionData[] actions;

    @TargetApi(18)
    public NotificationData(StatusBarNotification sbn) {
        Notification notification = sbn.getNotification();

        category = NotificationCompat.getCategory(notification);
        title = getTitle(notification);
        subtitle = getSubtitle(notification);
        packageName = sbn.getPackageName();
        priority = notification.priority;
        id = sbn.getId();

        icon = getIcon(notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unloadedIcon = notification.getSmallIcon();

        intent = notification.contentIntent;

        actions = new ActionData[NotificationCompat.getActionCount(notification)];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = new ActionData(NotificationCompat.getAction(notification, i), packageName);
        }
    }

    public NotificationData(Notification notification, String packageName) {
        category = NotificationCompat.getCategory(notification);
        title = getTitle(notification);
        subtitle = getSubtitle(notification);
        this.packageName = packageName;
        priority = notification.priority;
        id = -1;

        icon = getIcon(notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unloadedIcon = notification.getSmallIcon();

        intent = notification.contentIntent;

        actions = new ActionData[NotificationCompat.getActionCount(notification)];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = new ActionData(NotificationCompat.getAction(notification, i), packageName);
        }
    }

    public NotificationData(Notification notification, int id, String packageName) {
        category = NotificationCompat.getCategory(notification);
        title = getTitle(notification);
        subtitle = getSubtitle(notification);
        this.packageName = packageName;
        priority = notification.priority;
        this.id = id;

        icon = getIcon(notification);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unloadedIcon = notification.getSmallIcon();

        intent = notification.contentIntent;

        actions = new ActionData[NotificationCompat.getActionCount(notification)];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = new ActionData(NotificationCompat.getAction(notification, i), packageName);
        }
    }

    protected NotificationData(Parcel in) {
        category = in.readString();
        title = in.readString();
        subtitle = in.readString();
        packageName = in.readString();
        priority = in.readInt();
        id = in.readInt();
        icon = in.readInt();

        intent = PendingIntent.readPendingIntentOrNullFromParcel(in);

        int length = in.readInt();
        actions = new ActionData[length];
        for (int i = 0; i < length; i++) {
            actions[i] = in.readParcelable(ActionData.class.getClassLoader());
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unloadedIcon = in.readParcelable(Icon.class.getClassLoader());
    }

    public static final Creator<NotificationData> CREATOR = new Creator<NotificationData>() {
        @Override
        public NotificationData createFromParcel(Parcel in) {
            return new NotificationData(in);
        }

        @Override
        public NotificationData[] newArray(int size) {
            return new NotificationData[size];
        }
    };

    @Nullable
    public Drawable getIcon(Context context) {
        Drawable drawable = null;
        if (icon != 0) drawable = getDrawable(context, icon, packageName);
        if (drawable == null && unloadedIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            drawable = unloadedIcon.loadDrawable(context);

        return drawable;
    }

    public ActionData[] getActions() {
        return actions;
    }

    public String getName(Context context) {
        PackageManager packageManager = context.getPackageManager();
        ApplicationInfo application;
        try {
            application = packageManager.getApplicationInfo(packageName, 0);
        } catch (final PackageManager.NameNotFoundException e) {
            return category;
        }

        return packageManager.getApplicationLabel(application).toString();
    }

    public boolean shouldShowHeadsUp(Context context) {
        Boolean headsUp = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_HEADS_UP);
        return (headsUp != null ? headsUp : Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) && priority >= NotificationCompat.PRIORITY_HIGH;
    }

    public boolean shouldHideStatusBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && priority >= NotificationCompat.PRIORITY_HIGH;
    }

    public String getKey() {
        return String.valueOf(id) + "/" + packageName;
    }

    private String getTitle(Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);

        String title = extras.getString(NotificationCompat.EXTRA_TITLE);

        return title != null ? title : "";
    }

    private String getSubtitle(Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);

        String subtitle = extras.getString(NotificationCompat.EXTRA_TEXT);
        if (subtitle == null) subtitle = extras.getString(NotificationCompat.EXTRA_SUB_TEXT);
        if (subtitle == null && notification.tickerText != null)
            subtitle = notification.tickerText.toString();

        return subtitle != null ? subtitle : "";
    }

    private int getIcon(Notification notification) {
        Bundle extras = NotificationCompat.getExtras(notification);

        int icon = extras.getInt(NotificationCompat.EXTRA_SMALL_ICON, 0);
        if (icon == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M) icon = notification.icon;

        return icon;
    }

    @Nullable
    private Drawable getDrawable(Context context, int resource, String packageName) {
        if (packageName == null) return null;

        Resources resources = null;
        PackageInfo packageInfo = null;

        try {
            resources = context.getPackageManager().getResourcesForApplication(packageName);
            packageInfo = context.getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (resources == null || packageInfo == null) return null;

        Resources.Theme theme = resources.newTheme();
        theme.applyStyle(packageInfo.applicationInfo.theme, false);

        try {
            return ResourcesCompat.getDrawable(resources, resource, theme);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }

    public boolean equals(NotificationData obj) {
        return super.equals(obj) || (obj != null && obj.packageName.matches(packageName) && obj.id == id);
    }

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeString(category);
        dest.writeString(title);
        dest.writeString(subtitle);
        dest.writeString(packageName);
        dest.writeInt(priority);
        dest.writeInt(id);
        dest.writeInt(icon);

        PendingIntent.writePendingIntentOrNullToParcel(intent, dest);

        dest.writeInt(actions.length);
        for (ActionData action : actions) {
            dest.writeParcelable(action, 0);
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            dest.writeParcelable(unloadedIcon, flags);
    }
}
