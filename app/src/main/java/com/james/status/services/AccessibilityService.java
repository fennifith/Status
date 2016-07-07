package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.Resources;
import android.content.res.TypedArray;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.support.annotation.ColorInt;
import android.support.v4.content.ContextCompat;
import android.support.v7.graphics.Palette;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ImageUtils;
import com.james.status.utils.PreferenceUtils;

public class AccessibilityService extends android.accessibilityservice.AccessibilityService {

    @Override
    protected void onServiceConnected() {
        super.onServiceConnected();

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
            String homePackageName = getPackageManager().resolveActivity(homeIntent, PackageManager.MATCH_DEFAULT_ONLY).activityInfo.packageName;

            if (packageName.contains(homePackageName) || packageName.matches(homePackageName)) {
                Palette.from(ImageUtils.drawableToBitmap(WallpaperManager.getInstance(this).getFastDrawable())).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        setStatusBarColor(palette.getDarkVibrantColor(palette.getVibrantColor(Color.BLACK)));
                    }
                });

                return;
            }

            PackageInfo packageInfo = null;
            Resources resources = null;
            try {
                packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                resources = getPackageManager().getResourcesForApplication(packageInfo.applicationInfo);
            } catch (PackageManager.NameNotFoundException e) {
                e.printStackTrace();
            }

            if (packageInfo != null && resources != null) {
                Resources.Theme theme = resources.newTheme();
                theme.applyStyle(packageInfo.applicationInfo.theme, false);

                if (setStatusBarTheme(packageInfo.packageName, resources, theme)) return;

                if (packageInfo.activities != null) {
                    for (ActivityInfo activityInfo : packageInfo.activities) {
                        Resources.Theme activityTheme = resources.newTheme();
                        activityTheme.applyStyle(activityInfo.theme, false);
                        if (setStatusBarTheme(packageInfo.packageName, resources, activityTheme)) return;
                    }
                }

                Palette.from(ImageUtils.drawableToBitmap(getPackageManager().getApplicationIcon(packageInfo.applicationInfo))).generate(new Palette.PaletteAsyncListener() {
                    @Override
                    public void onGenerated(Palette palette) {
                        setStatusBarColor(palette.getDarkVibrantColor(palette.getVibrantColor(Color.BLACK)));
                    }
                });

                return;
            }

            setStatusBarColor(Color.BLACK);
        }
    }

    private boolean setStatusBarTheme(String packageName, Resources resources, Resources.Theme theme) {
        int[] attrs = new int[] {
                android.R.attr.colorPrimaryDark,
                android.R.attr.statusBarColor,
                android.R.attr.navigationBarColor,
                resources.getIdentifier("colorPrimaryDark", "attr", packageName),
                resources.getIdentifier("colorPrimaryDark", "color", packageName),
                android.R.attr.colorPrimary,
                resources.getIdentifier("colorPrimary", "attr", packageName),
                resources.getIdentifier("colorPrimary", "color", packageName)
        };

        TypedArray typedArray = theme.obtainStyledAttributes(attrs);
        for (int i = 0; i < typedArray.length(); i++) {
            int statusBarRes = typedArray.getResourceId(i, 0);
            if (statusBarRes != 0) {
                try {
                    setStatusBarColor(ContextCompat.getColor(this, statusBarRes));
                    return true;
                } catch (Resources.NotFoundException ignored) {
                }
            }
        }

        return false;
    }

    private void setStatusBarColor(@ColorInt int color) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);
        intent.putExtra(StatusService.EXTRA_COLOR, color);
        startService(intent);
    }

    @Override
    public void onInterrupt() {}
}
