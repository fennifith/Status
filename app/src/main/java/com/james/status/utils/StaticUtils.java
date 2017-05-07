package com.james.status.utils;

import android.Manifest;
import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import com.james.status.Status;
import com.james.status.activities.StartActivity;
import com.james.status.services.AccessibilityService;
import com.james.status.services.StatusService;

import java.util.ArrayList;
import java.util.List;

public class StaticUtils {

    public static int getStatusBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    public static boolean shouldShowTutorial(Context context, String tutorialName) {
        return shouldShowTutorial(context, tutorialName, 0);
    }

    public static boolean shouldShowTutorial(Context context, String tutorialName, int limit) {
        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        int shown = prefs.getInt("tutorial" + tutorialName, 0);
        prefs.edit().putInt("tutorial" + tutorialName, shown + 1).apply();
        return limit == shown;
    }

    public static int getNavigationBarHeight(Context context) {
        int resId = context.getResources().getIdentifier("navigation_bar_height", "dimen", "android");
        if (resId > 0) return context.getResources().getDimensionPixelSize(resId);
        else return 0;
    }

    public static float getPixelsFromDp(int dp) {
        return dp * Resources.getSystem().getDisplayMetrics().density;
    }

    public static float getDpFromPixels(int px) {
        return px / Resources.getSystem().getDisplayMetrics().density;
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
        return isAccessibilityServiceRunning(context);
    }

    public static boolean isNotificationGranted(Context context) {
        for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(context)) {
            if (packageName.contains(context.getPackageName()) || packageName.equals(context.getPackageName()))
                return true;
        }
        return shouldUseCompatNotifications(context);
    }

    public static boolean isIgnoringOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M)
            return ((PowerManager) context.getSystemService(Context.POWER_SERVICE)).isIgnoringBatteryOptimizations(context.getPackageName());
        else return true;
    }

    public static boolean canDrawOverlays(Context context) {
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
                    if (!permission.matches(Manifest.permission.SYSTEM_ALERT_WINDOW) && (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M || !permission.matches("android.permission.REQUEST_IGNORE_BATTERY_OPTIMIZATIONS")) && !permission.matches("android.permission.GET_TASKS")) {
                        Log.wtf("Permission", permission);
                        return false;
                    }
                }
            }
        }

        return true;
    }

    public static void requestPermissions(Activity activity) {
        PackageInfo info;
        try {
            info = activity.getPackageManager().getPackageInfo(activity.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return;
        }

        if (info.requestedPermissions != null) {
            List<String> unrequestedPermissions = new ArrayList<>();
            for (String permission : info.requestedPermissions) {
                if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED) {
                    Log.wtf("Permission", permission);
                    if (permission.length() > 0 && !permission.matches(Manifest.permission.SYSTEM_ALERT_WINDOW) && !permission.matches("android.permission.GET_TASKS"))
                        unrequestedPermissions.add(permission);
                }
            }

            if (unrequestedPermissions.size() > 0)
                ActivityCompat.requestPermissions(activity, unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]), StartActivity.REQUEST_PERMISSIONS);
        }
    }

    public static boolean isStatusServiceRunning(Context context) {
        Boolean isEnabled = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (isEnabled != null && isEnabled) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (StatusService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }

            Intent intent = new Intent(StatusService.ACTION_START);
            intent.setClass(context, StatusService.class);
            context.startService(intent);
            return true;
        } else return false;
    }

    public static void updateStatusService(Context context) {
        if (isStatusServiceRunning(context)) {
            Intent intent = new Intent(StatusService.ACTION_START);
            intent.setClass(context, StatusService.class);
            context.startService(intent);
        }

        ((Status) context.getApplicationContext()).onPreferenceChanged();
    }

    public static boolean isAccessibilityServiceRunning(Context context) {
        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (AccessibilityService.class.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
    }

    public static boolean shouldUseCompatNotifications(Context context) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_NOTIFICATIONS_COMPAT);
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 || (enabled != null && enabled);
    }
}
