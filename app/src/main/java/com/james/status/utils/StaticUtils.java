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

package com.james.status.utils;

import android.app.Activity;
import android.app.ActivityManager;
import android.bluetooth.BluetoothAdapter;
import android.bluetooth.BluetoothManager;
import android.content.Context;
import android.content.ContextWrapper;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.Build;
import android.os.PowerManager;
import android.preference.PreferenceManager;
import android.provider.Settings;
import android.util.Log;

import com.james.status.BuildConfig;
import com.james.status.activities.StartActivity;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.services.AccessibilityService;
import com.james.status.services.StatusServiceImpl;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.app.NotificationManagerCompat;
import androidx.core.content.ContextCompat;

public class StaticUtils {

    @Nullable
    public static AppCompatActivity getActivity(@Nullable Context context) {
        while (context instanceof ContextWrapper) {
            if (context instanceof AppCompatActivity)
                return (AppCompatActivity) context;
            else context = ((ContextWrapper) context).getBaseContext();
        }

        return null;
    }

    public static int getStatusBarHeight(Context context) {
        int height = PreferenceData.STATUS_HEIGHT.getValue(context);
        if (height > 0)
            return height;

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

    public static boolean isNotificationGranted(Context context) {
        for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(context)) {
            if (packageName.contains(context.getPackageName()) || packageName.equals(context.getPackageName()))
                return true;
        }
        return shouldUseCompatNotifications(context);
    }

    public static boolean isIgnoringOptimizations(Context context) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            PowerManager powerManager = (PowerManager) context.getSystemService(Context.POWER_SERVICE);
            if (powerManager != null)
                return powerManager.isIgnoringBatteryOptimizations(context.getPackageName());
        }

        return true;
    }

    public static boolean canDrawOverlays(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.M || Settings.canDrawOverlays(context);
    }

    public static int getMergedValue(int v1, int v2, float r) {
        return (int) ((v1 * r) + (v2 * (1 - r)));
    }

    public static boolean isPermissionsGranted(Context context, String[] permissions) {
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                if (BuildConfig.DEBUG)
                    Log.wtf("Permission", "missing " + permission);
                return PreferenceData.STATUS_IGNORE_PERMISSION_CHECKING.getValue(context);
            }
        }

        return true;
    }

    public static boolean isAllPermissionsGranted(Context context) {
        for (IconData icon : StatusServiceImpl.getIcons(context)) {
            for (String permission : icon.getPermissions()) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    if (BuildConfig.DEBUG)
                        Log.wtf("Permission", "missing " + permission);
                    return PreferenceData.STATUS_IGNORE_PERMISSION_CHECKING.getValue(context);
                }
            }
        }

        return true;
    }

    public static void requestPermissions(Activity activity, String[] permissions) {
        List<String> unrequestedPermissions = new ArrayList<>();
        for (String permission : permissions) {
            if (ContextCompat.checkSelfPermission(activity, permission) != PackageManager.PERMISSION_GRANTED)
                unrequestedPermissions.add(permission);
        }

        if (unrequestedPermissions.size() > 0)
            ActivityCompat.requestPermissions(activity, unrequestedPermissions.toArray(new String[unrequestedPermissions.size()]), StartActivity.REQUEST_PERMISSIONS);
    }

    public static boolean isStatusServiceRunning(Context context) {
        if (context != null && (boolean) PreferenceData.STATUS_ENABLED.getValue(context) && isReady(context)) {
            ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
            if (manager != null) {
                for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                    if (StatusServiceImpl.class.getName().equals(service.service.getClassName())) {
                        return true;
                    }
                }

                StatusServiceImpl.start(context);
                return true;
            }
        }

        return false;
    }

    /**
     * Sends an intent to apply preference changes to the StatusService
     *
     * @param context         current context to send intent from
     * @param shouldKeepIcons whether to reuse existing instances of IconDatas
     */
    public static void updateStatusService(Context context, boolean shouldKeepIcons) {
        if (isStatusServiceRunning(context)) {
            Intent intent = new Intent(StatusServiceImpl.ACTION_START);
            intent.setClass(context, StatusServiceImpl.getCompatClass(context));
            intent.putExtra(StatusServiceImpl.EXTRA_KEEP_OLD, shouldKeepIcons);
            context.startService(intent);
        }
    }

    public static boolean isAccessibilityServiceRunning(Context context) {
        if (context == null)
            return false;

        ActivityManager manager = (ActivityManager) context.getSystemService(Context.ACTIVITY_SERVICE);
        if (manager != null) {
            for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
                if (AccessibilityService.class.getName().equals(service.service.getClassName())) {
                    return true;
                }
            }
            return false;
        } else return true;
    }

    public static boolean shouldUseCompatNotifications(Context context) {
        return Build.VERSION.SDK_INT < Build.VERSION_CODES.JELLY_BEAN_MR2 || (boolean) PreferenceData.STATUS_NOTIFICATIONS_COMPAT.getValue(context);
    }

    public static boolean isReady(Context context) {
        return StaticUtils.isAccessibilityServiceRunning(context) && StaticUtils.isNotificationGranted(context) && StaticUtils.canDrawOverlays(context);
    }
}
