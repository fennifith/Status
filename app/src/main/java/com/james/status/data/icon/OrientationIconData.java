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

package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.res.Configuration;
import android.database.ContentObserver;
import android.os.Handler;
import android.provider.Settings;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.lang.ref.SoftReference;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class OrientationIconData extends IconData<OrientationIconData.OrientationReceiver> {

    private OrientationObserver observer;

    public OrientationIconData(Context context) {
        super(context);
    }

    @Override
    boolean isDefaultVisible() {
        return false;
    }

    @Override
    public OrientationReceiver getReceiver() {
        return new OrientationReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(Intent.ACTION_CONFIGURATION_CHANGED);
    }

    @Override
    public void register() {
        super.register();

        observer = new OrientationObserver(this);
        getContext().getContentResolver().registerContentObserver(Settings.System.getUriFor(Settings.System.ACCELEROMETER_ROTATION), true, observer);

        onDrawableUpdate();
    }

    @Override
    public void unregister() {
        if (observer != null) getContext().getContentResolver().unregisterContentObserver(observer);
        super.unregister();
    }

    private void onDrawableUpdate() {
        if (Settings.System.getInt(getContext().getContentResolver(), Settings.System.ACCELEROMETER_ROTATION, 0) == 1)
            onIconUpdate(0);
        else {
            int orientation = getContext().getResources().getConfiguration().orientation;
            switch (orientation) {
                case Configuration.ORIENTATION_PORTRAIT:
                    onIconUpdate(1);
                    break;
                case Configuration.ORIENTATION_LANDSCAPE:
                    onIconUpdate(2);
                    break;
                default:
                    onIconUpdate(-1);
                    break;
            }
        }
    }

    @Override
    public int getIconStyleSize() {
        return 3;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(Arrays.asList(
                new IconStyleData(
                        getContext().getString(R.string.icon_style_default),
                        IconStyleData.TYPE_VECTOR,
                        R.drawable.ic_orientation_auto,
                        R.drawable.ic_orientation_portrait,
                        R.drawable.ic_orientation_landscape
                ),
                new IconStyleData(
                        getContext().getString(R.string.icon_style_system),
                        IconStyleData.TYPE_IMAGE,
                        android.R.drawable.ic_menu_always_landscape_portrait,
                        android.R.drawable.ic_lock_idle_lock,
                        android.R.drawable.ic_lock_idle_lock
                )
        ));

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_orientation_auto),
                getContext().getString(R.string.icon_orientation_portrait),
                getContext().getString(R.string.icon_orientation_landscape)
        };
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_orientation);
    }

    static class OrientationReceiver extends IconUpdateReceiver<OrientationIconData> {

        public OrientationReceiver(OrientationIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(OrientationIconData icon, Intent intent) {
            icon.onDrawableUpdate();
        }
    }

    private static class OrientationObserver extends ContentObserver {

        private SoftReference<OrientationIconData> reference;

        private OrientationObserver(OrientationIconData iconData) {
            super(new Handler());
            reference = new SoftReference<>(iconData);
        }

        @Override
        public void onChange(boolean selfChange) {
            OrientationIconData iconData = reference.get();
            if (iconData != null) iconData.onDrawableUpdate();
        }
    }

}
