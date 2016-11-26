package com.james.status.data;

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;
import android.support.annotation.Nullable;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.res.ResourcesCompat;

public class ActionData extends NotificationCompat.Action implements Parcelable {

    private String packageName;

    public ActionData(NotificationCompat.Action action, String packageName) {
        super(action.icon, action.title, action.actionIntent);
    }

    protected ActionData(Parcel in) {
        super(in.readInt(), in.readString(), PendingIntent.readPendingIntentOrNullFromParcel(in));
        packageName = in.readString();
    }

    public static final Creator<ActionData> CREATOR = new Creator<ActionData>() {
        @Override
        public ActionData createFromParcel(Parcel in) {
            return new ActionData(in);
        }

        @Override
        public ActionData[] newArray(int size) {
            return new ActionData[size];
        }
    };

    @Override
    public int describeContents() {
        return 0;
    }

    @Override
    public void writeToParcel(Parcel dest, int flags) {
        dest.writeInt(getIcon());
        dest.writeString(getTitle() != null ? getTitle().toString() : "");
        PendingIntent.writePendingIntentOrNullToParcel(getActionIntent(), dest);
        dest.writeString(packageName);
    }

    @Nullable
    public Drawable getIcon(Context context) {
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
            return ResourcesCompat.getDrawable(resources, getIcon(), theme);
        } catch (Resources.NotFoundException e) {
            return null;
        }
    }
}
