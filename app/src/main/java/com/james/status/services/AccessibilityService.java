package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.WallpaperManager;
import android.content.ComponentName;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.res.ResourcesCompat;
import android.support.v7.graphics.Palette;
import android.view.WindowManager;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    private PackageManager packageManager;

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

        packageManager = getPackageManager();

        AccessibilityServiceInfo config = new AccessibilityServiceInfo();
        config.eventTypes = AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED;
        config.feedbackType = AccessibilityServiceInfo.FEEDBACK_GENERIC;

        if (Build.VERSION.SDK_INT >= 16) config.flags = AccessibilityServiceInfo.FLAG_INCLUDE_NOT_IMPORTANT_VIEWS;

        setServiceInfo(config);
    }

    @Override
    public void onAccessibilityEvent(AccessibilityEvent event) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            String packageName = event.getPackageName().toString();

            Intent homeIntent = new Intent(Intent.ACTION_MAIN);
            homeIntent.addCategory(Intent.CATEGORY_HOME);
            String homePackageName = packageManager.resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;

            if (packageName.contains(homePackageName) || packageName.matches(homePackageName)) {
                Palette.from(ImageUtils.drawableToBitmap(WallpaperManager.getInstance(this).getFastDrawable())).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        setStatusBar(palette.getDarkVibrantColor(ImageUtils.darkColor(palette.getVibrantColor(Color.BLACK))));
                    }
                });

                return;
            }

            ComponentName componentName = new ComponentName(packageName, event.getClassName().toString());

            ActivityInfo activityInfo = null;
            PackageInfo packageInfo = null;
            Resources resources = null, activityResources = null;
            try {
                packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_META_DATA);
                resources = packageManager.getResourcesForApplication(packageInfo.applicationInfo);
                activityInfo = packageManager.getActivityInfo(componentName, 0);
                activityResources = packageManager.getResourcesForActivity(componentName);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (packageInfo != null && resources != null) {
                if (activityInfo != null && activityResources != null) {
                    Resources.Theme theme = resources.newTheme();
                    theme.applyStyle(activityInfo.theme, false);

                    Integer statusBarColor = getStatusBarColor(activityInfo.packageName, resources, theme);
                    if (statusBarColor != null) {
                        setStatusBar(statusBarColor, activityInfo.flags == WindowManager.LayoutParams.FLAG_FULLSCREEN);
                        return;
                    }
                }

                Resources.Theme theme = resources.newTheme();
                theme.applyStyle(packageInfo.applicationInfo.theme, false);

                Integer statusBarColor = getStatusBarColor(packageInfo.packageName, resources, theme);
                if (statusBarColor != null) {
                    setStatusBar(statusBarColor, packageInfo.applicationInfo.flags == WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    return;
                }

                if (packageInfo.activities != null) {
                    for (ActivityInfo otherActivityInfo : packageInfo.activities) {
                        Resources.Theme otherTheme = resources.newTheme();
                        otherTheme.applyStyle(otherActivityInfo.theme, false);

                        Integer otherStatusBarColor = getStatusBarColor(packageInfo.packageName, resources, otherTheme);
                        if (otherStatusBarColor != null) {
                            setStatusBar(otherStatusBarColor, packageInfo.applicationInfo.flags == WindowManager.LayoutParams.FLAG_FULLSCREEN);
                            return;
                        }
                    }
                }

                Palette.from(ImageUtils.drawableToBitmap(getPackageManager().getApplicationIcon(packageInfo.applicationInfo))).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        setStatusBar(palette.getDarkVibrantColor(ImageUtils.darkColor(palette.getVibrantColor(Color.BLACK))));
                    }
                });

                return;
            }

            setStatusBar(Color.BLACK);
        }
    }

    private Integer getStatusBarColor(String packageName, Resources resources, Resources.Theme theme) {
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
                    return ImageUtils.darkColor(ResourcesCompat.getColor(resources, statusBarRes, theme));
                } catch (Resources.NotFoundException ignored) {
                }
            }
        }

        return null;
    }

    private void setStatusBar(@ColorInt int color) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto == null || isStatusColorAuto)
            intent.putExtra(StatusService.EXTRA_COLOR, color);

        startService(intent);
    }

    private void setStatusBar(@ColorInt int color, boolean fullscreen) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto == null || isStatusColorAuto)
            intent.putExtra(StatusService.EXTRA_COLOR, color);

        intent.putExtra(StatusService.EXTRA_FULLSCREEN, fullscreen);

        startService(intent);
    }

    @Override
    public void onInterrupt() {}
}
