package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.WallpaperManager;
import android.content.Intent;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.content.res.TypedArray;
import android.graphics.Color;
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
                        setStatusBarColor(palette.getDarkVibrantColor(Color.BLACK));
                    }
                });
            } else {
                PackageInfo packageInfo = null;
                try {
                    packageInfo = getPackageManager().getPackageInfo(packageName, PackageManager.GET_META_DATA);
                } catch (PackageManager.NameNotFoundException e) {
                    e.printStackTrace();
                }

                if (packageInfo != null) {
                    TypedArray typedColorPrimaryDark = getTheme().obtainStyledAttributes(packageInfo.applicationInfo.theme, new int[]{android.R.attr.colorPrimaryDark});
                    int statusBarColor = typedColorPrimaryDark.getResourceId(0, 0);
                    typedColorPrimaryDark.recycle();

                    if (statusBarColor != 0) {
                        setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
                    } else {
                        TypedArray typedStatusBarColor = getTheme().obtainStyledAttributes(packageInfo.applicationInfo.theme, new int[]{android.R.attr.statusBarColor});
                        statusBarColor = typedStatusBarColor.getResourceId(0, 0);
                        typedStatusBarColor.recycle();

                        if (statusBarColor != 0) {
                            setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
                        } else {
                            TypedArray typedColorPrimary = getTheme().obtainStyledAttributes(packageInfo.applicationInfo.theme, new int[]{android.R.attr.colorPrimary});
                            statusBarColor = ImageUtils.darkColor(typedColorPrimary.getResourceId(0, 0));
                            typedColorPrimary.recycle();

                            if (statusBarColor != 0) {
                                setStatusBarColor(ContextCompat.getColor(this, statusBarColor));
                            }
                        }
                    }
                } else {
                    setStatusBarColor(Color.BLACK);
                }
            }
        }
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
