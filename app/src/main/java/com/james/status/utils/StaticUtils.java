package com.james.status.utils;

import android.app.ActivityManager;
import android.content.Context;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Build;
import android.preference.PreferenceManager;
import android.support.v4.app.NotificationManagerCompat;
import android.support.v4.content.ContextCompat;
import android.util.TypedValue;

import com.james.status.services.StatusService;

public class StaticUtils {

    public static int getStatusBarMargin(Context context) {
        int height = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 25, context.getResources().getDisplayMetrics());
        int resId = context.getResources().getIdentifier("status_bar_height", "dimen", "android");
        if (resId > 0) height = context.getResources().getDimensionPixelSize(resId);
        return height;
    }

    public static boolean isAccessibilityGranted(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("isResumed", false);
    }

    public static boolean isNotificationGranted(Context context) {
        for (String packageName : NotificationManagerCompat.getEnabledListenerPackages(context)) {
            if (packageName.contains(context.getPackageName()) || packageName.matches(context.getPackageName())) return true;
        }
        return false;
    }

    public static boolean isPermissionsGranted(Context context) {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) return true;

        PackageInfo info;
        try {
            info = context.getPackageManager().getPackageInfo(context.getPackageName(), PackageManager.GET_PERMISSIONS);
        } catch (PackageManager.NameNotFoundException e) {
            e.printStackTrace();
            return false;
        }

        if (info.requestedPermissions != null) {
            for (String permission : info.requestedPermissions) {
                if (ContextCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) return false;
            }
        }
        return true;
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
