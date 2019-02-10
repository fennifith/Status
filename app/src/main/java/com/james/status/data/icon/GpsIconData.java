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

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;

public class GpsIconData extends IconData<GpsIconData.GpsReceiver> {

    private static final String
            GPS_ENABLED_CHANGE_ACTION = "android.location.GPS_ENABLED_CHANGE",
            GPS_FIX_CHANGE_ACTION = "android.location.GPS_FIX_CHANGE";

    private LocationManager locationManager;

    public GpsIconData(Context context) {
        super(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public GpsReceiver getReceiver() {
        return new GpsReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        IntentFilter intentFilter = new IntentFilter(GPS_ENABLED_CHANGE_ACTION);
        intentFilter.addAction(GPS_FIX_CHANGE_ACTION);
        return intentFilter;
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_gps);
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.ACCESS_COARSE_LOCATION, Manifest.permission.ACCESS_FINE_LOCATION};
    }

    @Override
    public int getIconStyleSize() {
        return 2;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_gps_searching,
                                R.drawable.ic_gps_fixed
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_system),
                                IconStyleData.TYPE_IMAGE,
                                android.R.drawable.ic_menu_mapmode,
                                android.R.drawable.ic_menu_mylocation
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_dish),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_gps_dish_searching,
                                R.drawable.ic_gps_dish_fixed
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_dish2),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_gps_dish2_searching",
                                "ic_gps_dish2_fixed"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_satellite),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_gps_satellite_searching",
                                "ic_gps_satellite_fixed"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_satellite_outline),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_gps_satellite_outline_searching",
                                "ic_gps_satellite_outline_fixed"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_pin),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_gps_pin_searching",
                                "ic_gps_pin_fixed"
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_gps_searching),
                getContext().getString(R.string.icon_gps_fixed)
        };
    }

    static class GpsReceiver extends IconUpdateReceiver<GpsIconData> {

        private GpsReceiver(GpsIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(GpsIconData icon, Intent intent) {
            if (icon.locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (isLocationFixed(icon)) {
                    icon.onIconUpdate(1);
                } else
                    icon.onIconUpdate(0);
            } else icon.onIconUpdate(-1);
        }

        private boolean isLocationFixed(GpsIconData icon) {
            if (ContextCompat.checkSelfPermission(icon.getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = icon.locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                return location != null && (getElapsedTime(location) < 3000 || location.hasAccuracy());
            } else return false;
        }

        private long getElapsedTime(@NonNull Location location) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                return location.getElapsedRealtimeNanos() - SystemClock.elapsedRealtimeNanos();
            else return location.getTime() - System.currentTimeMillis();
        }
    }
}
