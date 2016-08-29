package com.james.status.utils;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.res.ResourcesCompat;

import com.google.gson.Gson;
import com.james.status.data.ActivityColorData;

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

    @Nullable
    public static Integer getPrimaryColor(Context context, ComponentName componentName) {
        PackageManager packageManager = context.getPackageManager();

        ActivityInfo activityInfo = null;
        PackageInfo packageInfo = null;
        Resources resources = null, activityResources = null;
        try {
            packageInfo = packageManager.getPackageInfo(componentName.getPackageName(), PackageManager.GET_META_DATA);
            resources = packageManager.getResourcesForApplication(packageInfo.applicationInfo);
            activityInfo = packageManager.getActivityInfo(componentName, 0);
            activityResources = packageManager.getResourcesForActivity(componentName);
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        if (packageInfo != null && resources != null) {
            Integer statusBarColor = getStatusBarColor(packageInfo.packageName, resources, packageInfo.applicationInfo.theme);
            if (statusBarColor != null) {
                return statusBarColor;
            }

            if (activityInfo != null && activityResources != null) {
                Integer activityStatusBarColor = getStatusBarColor(activityInfo.packageName, resources, activityInfo.theme);
                if (activityStatusBarColor != null) {
                    return activityStatusBarColor;
                }
            }

            if (packageInfo.activities != null) {
                for (ActivityInfo otherActivityInfo : packageInfo.activities) {
                    Integer otherStatusBarColor = getStatusBarColor(packageInfo.packageName, resources, otherActivityInfo.theme);
                    if (otherStatusBarColor != null) {
                        return otherStatusBarColor;
                    }
                }
            }
        }

        return null;
    }

    public static Integer getStatusBarColor(Context context, ComponentName componentName, @ColorInt Integer defaultColor) {
        Set<String> apps = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_APPS);
        if (apps != null) {
            Gson gson = new Gson();
            for (String app : apps) {
                ActivityColorData data = gson.fromJson(app, ActivityColorData.class);
                if (componentName.getPackageName().matches(data.packageName) && (data.name == null || componentName.getClassName().matches(data.name)) && data.color != null) {
                    return data.color;
                }
            }
        }

        Integer color = getPrimaryColor(context, componentName);
        return color != null ? color : defaultColor;
    }

    public static Integer getStatusBarColor(String packageName, Resources resources, int style) {
        Resources.Theme theme = resources.newTheme();
        theme.applyStyle(style, true);

        TypedArray typedArray = theme.obtainStyledAttributes(style, new int[]{
                resources.getIdentifier("colorPrimaryDark", "attr", packageName),
                resources.getIdentifier("statusBarColor", "attr", packageName),
                resources.getIdentifier("colorPrimaryDark", "color", packageName)
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

        typedArray = theme.obtainStyledAttributes(style, new int[]{
                resources.getIdentifier("colorPrimary", "attr", packageName),
                resources.getIdentifier("colorPrimary", "color", packageName),
                resources.getIdentifier("navigationBarColor", "attr", packageName)
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

    @ColorInt
    public static int getAverageColor(@NonNull Bitmap bitmap) {
        int red = 0, green = 0, blue = 0;
        int width = bitmap.getWidth(), height = bitmap.getHeight(), size = width * height;

        int[] pixels = new int[size];
        bitmap.getPixels(pixels, 0, bitmap.getWidth(), 0, 0, bitmap.getWidth(), bitmap.getHeight());

        for (int y = 0; y < height; y++) {
            for (int x = 0; x < width; x++) {
                int color = pixels[x + y * width];
                red += (color >> 16) & 0xFF;
                green += (color >> 8) & 0xFF;
                blue += (color & 0xFF);
            }
        }

        return Color.argb(255, red / size, green / size, blue / size);
    }
}
