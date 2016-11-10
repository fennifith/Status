package com.james.status.data.icon;

import android.Manifest;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.SystemClock;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.Arrays;
import java.util.List;

public class GpsIconData extends IconData<GpsIconData.GpsReceiver> {

    private static final String
            GPS_ENABLED_CHANGE_ACTION = "android.location.GPS_ENABLED_CHANGE",
            GPS_FIX_CHANGE_ACTION = "android.location.GPS_FIX_CHANGE";

    LocationManager locationManager;

    public GpsIconData(Context context) {
        super(context);
        locationManager = (LocationManager) context.getSystemService(Context.LOCATION_SERVICE);
    }

    @Override
    public GpsReceiver getReceiver() {
        return new GpsReceiver();
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
                                getContext().getString(R.string.icon_style_dish),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_gps_dish_searching,
                                R.drawable.ic_gps_dish_fixed
                        )
                )
        );

        return styles;
    }

    public class GpsReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            if (locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {
                if (isLocationFixed()) {
                    onDrawableUpdate(1);
                } else
                    onDrawableUpdate(0);
            } else onDrawableUpdate(-1);
        }

        private boolean isLocationFixed() {
            if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
                Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
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
