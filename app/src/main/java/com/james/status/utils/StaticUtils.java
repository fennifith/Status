package com.james.status.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Build;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;
import android.util.TypedValue;

import com.james.status.services.StatusService;

import java.util.ArrayList;
import java.util.List;

public class StaticUtils {

    public static int getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    public static int getNavigationBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    public static float getPixelsFromDp(Context context, int dp) {
        return TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, dp, context.getResources().getDisplayMetrics());
    }

    public static int getBluetoothState(Context context) {
        BluetoothAdapter adapter = BluetoothAdapter.getDefaultAdapter();
        if (adapter != null) return adapter.getState();
        else {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN_MR2)
                adapter = ((BluetoothManager) context.getSystemService(Context.BLUETOOTH_SERVICE)).getAdapter();
            if (adapter != null) return adapter.getState();
            else return BluetoothAdapter.STATE_OFF;
        }
    }

    public static boolean isAccessibilityGranted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isResumed", false);
    }

    public static boolean isNotificationGranted(Context context) {
        for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(context)) {
            if (packageName.contains(context.getPackageName()) || packageName.matches(context.getPackageName())) return true;
        }
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2;
    }

    private static boolean canDrawOverlays(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;
        else return Settings.canDrawOverlays(context);
    }

    public static boolean isPermissionsGranted(Context context) {
        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (info.requestedPermissions != null) {
            for (String permission : info.requestedPermissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.wtf("Permission", permission);
                    if (!permission.matches(Manifest.permission.SYSTEM_ALERT_WINDOW) || !canDrawOverlays(context))
                        return false;
                }
            }
        }

        return true;
    }

    public static boolean isPermissionsGranted(Activity activity, boolean shouldRequestPermissions) {
        PackageInfo info;
        try {
            info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (info.requestedPermissions != null) {
            List<String> unrequestedPermissions = new ArrayList<>();
            for (String permission : info.requestedPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.wtf("Permission", permission);
                    if (!permission.matches(Manifest.permission.SYSTEM_ALERT_WINDOW))
                        unrequestedPermissions.add(permission);
                }
            }

            if (shouldRequestPermissions) {
                String[] permissions = new String[unrequestedPermissions.size()];
                for (int i = 0; i < unrequestedPermissions.size(); i++) {
                    permissions[i] = unrequestedPermissions.get(i);
                }

                ActivityCompat.requestPermissions(activity, permissions, 9374);

                if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
                    if (!canDrawOverlays(activity))
                        activity.startActivityForResult(new Intent(Settings.ACTION_MANAGE_OVERLAY_PERMISSION, Uri.parse("package:" + activity.getPackageName())), 9374);
                }
            }
        }

        return isPermissionsGranted(activity);
    }

    public static boolean isStatusServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (StatusService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }
}
