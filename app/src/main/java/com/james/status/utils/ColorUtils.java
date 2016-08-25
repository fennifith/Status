package com.james.status.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;

import com.google.gson.Gson;
import com.james.status.data.AppColorData;

import java.util.Set;

public class ColorUtils {

    public static boolean isColorDark(int color) {
        return (0.299 * Color.red(color) + 0.587 * Color.green(color) + 0.114 * Color.blue(color)) / 255 < 0.5;
    }

    public static int darkColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] *= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static int lightColor(int color) {
        float[] hsv = new float[3];
        Color.colorToHSV(color, hsv);
        hsv[2] /= 0.8f;
        return Color.HSVToColor(hsv);
    }

    public static int muteColor(int color, int variant) {
        int mutedColor = Color.argb(255, (int) (127.5 + Color.red(color)) / 2, (int) (127.5 + Color.green(color)) / 2, (int) (127.5 + Color.blue(color)) / 2);
        switch (variant % 3) {
            case 1:
                return Color.argb(255, Color.red(mutedColor) + 10, Color.green(mutedColor) + 10, Color.blue(mutedColor) + 10);
            case 2:
                return Color.argb(255, Color.red(mutedColor) - 10, Color.green(mutedColor) - 10, Color.blue(mutedColor) - 10);
            default:
                return mutedColor;
        }
    }

    public static int getStatusBarColor(Context context, PackageManager packageManager, String packageName) {
        Set<String> apps = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_APPS);
        if (apps != null) {
            Gson gson = new Gson();
            for (String app : apps) {
                AppColorData data = gson.fromJson(app, AppColorData.class);
                if (packageName.matches(data.packageName) && data.color != null) {
                    return data.color;
                }
            }
        }

        ComponentName componentName = new ComponentName(packageName, packageName);

        ActivityInfo activityInfo = null;
        PackageInfo packageInfo = null;
        Resources resources = null, activityResources = null;
        try {
            packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
            resources = packageManager.getResourcesForApplication(packageInfo.applicationInfo);
            activityInfo = packageManager.getActivityInfo(componentName, 0);
            activityResources = packageManager.getResourcesForActivity(componentName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (packageInfo != null && resources != null) {
            Resources.Theme theme = resources.newTheme();
            theme.applyStyle(packageInfo.applicationInfo.theme, false);

            Integer statusBarColor = getStatusBarColor(packageInfo.packageName, resources, theme);
            if (statusBarColor != null) {
                return statusBarColor;
            }

            if (activityInfo != null && activityResources != null) {
                Resources.Theme activityTheme = resources.newTheme();
                activityTheme.applyStyle(activityInfo.theme, false);

                Integer activityStatusBarColor = getStatusBarColor(activityInfo.packageName, resources, activityTheme);
                if (activityStatusBarColor != null) {
                    return activityStatusBarColor;
                }
            }

            if (packageInfo.activities != null) {
                for (ActivityInfo otherActivityInfo : packageInfo.activities) {
                    Resources.Theme otherTheme = resources.newTheme();
                    otherTheme.applyStyle(otherActivityInfo.theme, false);

                    Integer otherStatusBarColor = getStatusBarColor(packageInfo.packageName, resources, otherTheme);
                    if (otherStatusBarColor != null) {
                        return otherStatusBarColor;
                    }
                }
            }

            Palette palette = Palette.from(ImageUtils.drawableToBitmap(packageManager.getApplicationIcon(packageInfo.applicationInfo))).generate();
            return palette.getDarkVibrantColor(darkColor(palette.getVibrantColor(Color.BLACK)));
        }

        return Color.BLACK;
    }

    public static Integer getStatusBarColor(String packageName, Resources resources, Resources.Theme theme) {
        TypedArray typedArray = theme.obtainStyledAttributes(new int[]{
                android.R.attr.colorPrimaryDark,
                android.R.attr.statusBarColor,
                android.R.attr.navigationBarColor,
                resources.getIdentifier("colorPrimaryDark", "attr", packageName),
                resources.getIdentifier("colorPrimaryDark", "color", packageName),
        });

        for (int i = 0; i < typedArray.length(); i++) {
            int statusBarRes = typedArray.getResourceId(i, 0);
            if (statusBarRes != 0) {
                try {
                    return ResourcesCompat.getColor(resources, statusBarRes, theme);
                } catch (Resources.NotFoundException ignored) {
                }
            }
        }

        typedArray = theme.obtainStyledAttributes(new int[]{
                android.R.attr.colorPrimary,
                resources.getIdentifier("colorPrimary", "attr", packageName),
                resources.getIdentifier("colorPrimary", "color", packageName)
        });

        for (int i = 0; i < typedArray.length(); i++) {
            int statusBarRes = typedArray.getResourceId(i, 0);
            if (statusBarRes != 0) {
                try {
                    return darkColor(ResourcesCompat.getColor(resources, statusBarRes, theme));
                } catch (Resources.NotFoundException ignored) {
                }
            }
        }

        return null;
    }
}
