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

package com.james.status.data;

import android.annotation.TargetApi;
import android.app.Notification;
import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.Icon;
import android.os.Build;
import android.os.Bundle;
import android.os.Parcel;
import android.os.Parcelable;
import android.service.notification.StatusBarNotification;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;
import me.jfenn.androidutils.ImageUtils;
import me.jfenn.androidutils.anim.AnimatedFloat;

public class NotificationData implements Parcelable {

    public String category, title, subtitle, packageName, group, key, tag = "";
    public int priority, id, iconRes, color = Color.BLACK;
    private boolean isAlert, isOngoing;
    private Bitmap icon, scaledIcon;
    private Bitmap largeIcon;
    private Icon unloadedIcon, unloadedLargeIcon;

    private AnimatedFloat scale;

    public PendingIntent intent;
    public ActionData[] actions;

    @TargetApi(18)
    public NotificationData(StatusBarNotification sbn, String key) {
        this.key = key;
        init(sbn.getNotification(), sbn.getId(), sbn.getPackageName());
        isOngoing = sbn.isOngoing();
    }

    public NotificationData(Notification notification, String packageName) {
        init(notification, -1, packageName);
    }

    public NotificationData(Notification notification, int id, String packageName) {
        init(notification, id, packageName);
    }

    /**
     * Instantiate the NotificationData from whatever information there is.
     *
     * @param notification The Notification that was sent.
     * @param id           An "id" that may or may not be tied to the
     *                     notification - who knows.
     * @param packageName  The package name that the notification _might_
     *                     have been from - we're only guessing here, after
     *                     all.
     */
    private void init(Notification notification, int id, String packageName) {
        category = NotificationCompat.getCategory(notification);
        title = getTitle(notification);
        subtitle = getSubtitle(notification);
        this.packageName = packageName;
        group = NotificationCompat.getGroup(notification);
        priority = notification.priority;
        this.id = id;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) this.color = notification.color;

        isAlert = notification.vibrate != null || notification.sound != null;

        Bundle extras = NotificationCompat.getExtras(notification);

        iconRes = extras.getInt(NotificationCompat.EXTRA_SMALL_ICON, 0);
        if (iconRes == 0 && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            iconRes = notification.icon;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            try {
                if (iconRes == 0)
                    iconRes = extras.getInt(NotificationCompat.EXTRA_SMALL_ICON, 0);
            } catch (Exception ignored) {
            }

            try {
                unloadedIcon = extras.getParcelable(NotificationCompat.EXTRA_SMALL_ICON);
            } catch (Exception ignored) {
            }

            if (unloadedIcon == null)
                unloadedIcon = notification.getSmallIcon();
        }

        Object parcelable = extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON);
        if (parcelable != null) {
            if (parcelable instanceof Bitmap) largeIcon = (Bitmap) parcelable;
            else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && parcelable instanceof Icon) { //throws a ClassNotFoundException? :P
                        unloadedLargeIcon = (Icon) parcelable;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (largeIcon == null) {
            parcelable = extras.getParcelable(NotificationCompat.EXTRA_LARGE_ICON_BIG);

            if (parcelable instanceof Bitmap) largeIcon = (Bitmap) parcelable;
            else {
                try {
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M && parcelable instanceof Icon) { //throws a ClassNotFoundException? :P
                        unloadedLargeIcon = (Icon) parcelable;
                    }
                } catch (Exception ignored) {
                }
            }
        }

        if (largeIcon == null && Build.VERSION.SDK_INT < Build.VERSION_CODES.M)
            largeIcon = notification.largeIcon;

        if (largeIcon != null && largeIcon.getByteCount() > 1000)
            largeIcon = null;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            unloadedLargeIcon = notification.getLargeIcon();

        intent = notification.contentIntent;

        actions = new ActionData[NotificationCompat.getActionCount(notification)];
        for (int i = 0; i < actions.length; i++) {
            actions[i] = new ActionData(NotificationCompat.getAction(notification, i), packageName);
        }
    }

    /**
     * Change the attributes of this NotificationData to match those of
     * another newly received notification. This should only be called
     * when we are _sure_ that the two notifications are, er, the same.
     *
     * @param notification              The new notification, that is,
     *                                  the new version of the notification
     *                                  that this notification is.
     * @return True if anything changed.
     */
    public boolean set(NotificationData notification) {
        boolean isChange = false;

        if (iconRes != notification.iconRes) {
            iconRes = notification.iconRes;
            icon = null;
            isChange = true;
        }

        if ((unloadedIcon == null && notification.unloadedIcon != null) || (unloadedIcon != null && !unloadedIcon.equals(notification.unloadedIcon))) {
            unloadedIcon = notification.unloadedIcon;
            icon = null;
            isChange = true;
        }

        return isChange;
    }

    protected NotificationData(Parcel in) {
        category = in.readString();
        title = in.readString();
        subtitle = in.readString();
        packageName = in.readString();
        group = in.readString();
        key = in.readString();
        tag = in.readString();
        priority = in.readInt();
        id = in.readInt();
        color = in.readInt();
        iconRes = in.readInt();
        isAlert = in.readByte() == 1;
        if (isAlert) {
            if (in.readByte() == 1) largeIcon = Bitmap.CREATOR.createFromParcel(in);

            intent = PendingIntent.readPendingIntentOrNullFromParcel(in);

            int length = in.readInt();
            actions = new ActionData[length];
            for (int i = 0; i < length; i++) {
                actions[i] = in.readParcelable(ActionData.class.getClassLoader());
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                unloadedIcon = in.readParcelable(Icon.class.getClassLoader());
                unloadedLargeIcon = in.readParcelable(Icon.class.getClassLoader());
            }
        }
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

    public boolean isOngoing() {
        return isOngoing;
    }

    /**
     * Get an icon bitmap with a specified height. If the current
     * bitmap matches the height, great! if not, it's scaled to
     * match it (and a reference to the scaled version is kept
     * as "cache").
     *
     * @param height            The height to generate a bitmap at.
     * @return The created bitmap, or null if things
     *                          went wrong.
     */
    @Nullable
    public Bitmap getIcon(float height) {
        if (icon != null && icon.getHeight() == Math.round(height))
            return icon;

        if (icon != null && (scaledIcon == null || scaledIcon.getHeight() != Math.round(height)))
            scaledIcon = Bitmap.createScaledBitmap(icon, Math.round(height * icon.getWidth() / icon.getHeight()), Math.round(height), true);

        return scaledIcon;
    }

    /**
     * Get a full res icon of the notification.
     *
     * @param context           The current application context.
     * @return The created bitmap, or null if
     *                          things went wrong.
     */
    @Nullable
    public Bitmap getIcon(Context context) {
        if (icon == null) {
            Drawable drawable = null;
            if (iconRes != 0) drawable = getDrawable(context, iconRes, packageName);
            if (drawable == null && unloadedIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
                drawable = unloadedIcon.loadDrawable(context);

            if (drawable != null) {
                Bitmap bitmap = ImageUtils.drawableToBitmap(drawable);
                if (bitmap != null) {
                    icon = bitmap;
                    scaledIcon = null;
                }
            }
        }
        return icon;
    }

    @Nullable
    public Drawable getLargeIcon(Context context) {
        Drawable drawable = null;
        if (largeIcon != null) drawable = new BitmapDrawable(context.getResources(), largeIcon);
        if (drawable == null && unloadedLargeIcon != null && Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            drawable = unloadedLargeIcon.loadDrawable(context);

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
        return //(boolean) PreferenceData.STATUS_NOTIFICATIONS_HEADS_UP.getValue(context) && TODO: #137
                (priority >= NotificationCompat.PRIORITY_HIGH || Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) && isAlert;
    }

    public boolean shouldHideStatusBar() {
        return Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP && priority >= NotificationCompat.PRIORITY_HIGH && isAlert;
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
        } catch (Resources.NotFoundException | OutOfMemoryError e) {
            return null;
        }
    }

    public AnimatedFloat getScale() {
        if (scale == null) {
            scale = new AnimatedFloat(0);
            scale.to(1f);
        }

        return scale;
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
        dest.writeString(group);
        dest.writeString(key);
        dest.writeString(tag);
        dest.writeInt(priority);
        dest.writeInt(id);
        dest.writeInt(color);
        dest.writeInt(iconRes);
        dest.writeByte((byte) (isAlert ? 1 : 0));
        if (isAlert) {
            if (largeIcon != null) {
                dest.writeByte((byte) 1);
                largeIcon.writeToParcel(dest, flags);
            } else dest.writeByte((byte) 0);

            PendingIntent.writePendingIntentOrNullToParcel(intent, dest);

            dest.writeInt(actions.length);
            for (ActionData action : actions) {
                dest.writeParcelable(action, flags);
            }

            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                dest.writeParcelable(unloadedIcon, flags);
                dest.writeParcelable(unloadedLargeIcon, flags);
            }
        }
    }
}
