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

import android.app.PendingIntent;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.os.Parcel;
import android.os.Parcelable;

import androidx.annotation.Nullable;
import androidx.core.app.NotificationCompat;
import androidx.core.content.res.ResourcesCompat;

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
