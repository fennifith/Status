package com.james.status.services;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.view.accessibility.AccessibilityEvent;

import com.james.status.utils.ColorUtils;
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
    public void onAccessibilityEvent(final AccessibilityEvent event) {
        Boolean enabled = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_ENABLED);
        if (enabled != null && enabled && event.getEventType() == AccessibilityEvent.TYPE_WINDOW_STATE_CHANGED) {
            final CharSequence packageName = event.getPackageName();
            if (packageName != null && packageName.length() > 0) {
                if (packageName.toString().equals("com.android.systemui") && event.getClassName().toString().equals("android.widget.FrameLayout")) {
                    setStatusBar(false);
                } else if (packageName.toString().equals("com.android.systemui")) {
                    setStatusBar(Color.BLACK, true);
                } else {
                    new Thread() {
                        @Override
                        public void run() {
                            final int color = ColorUtils.getStatusBarColor(AccessibilityService.this, packageManager, packageName.toString());

                            new Handler(getMainLooper()).post(new Runnable() {
                                @Override
                                public void run() {
                                    setStatusBar(color);
                                }
                            });
                        }
                    }.start();
                }
            }
        }
    }

    private void setStatusBar(@ColorInt int color) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto == null || isStatusColorAuto)
            intent.putExtra(StatusService.EXTRA_COLOR, color);

        startService(intent);
    }

    private void setStatusBar(@ColorInt int color, boolean shown) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        Boolean isStatusColorAuto = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_AUTO);
        if (isStatusColorAuto == null || isStatusColorAuto)
            intent.putExtra(StatusService.EXTRA_COLOR, color);

        intent.putExtra(StatusService.EXTRA_FULLSCREEN, !shown);

        startService(intent);
    }

    private void setStatusBar(boolean shown) {
        Intent intent = new Intent(StatusService.ACTION_UPDATE);
        intent.setClass(this, StatusService.class);

        intent.putExtra(StatusService.EXTRA_FULLSCREEN, !shown);

        startService(intent);
    }

    @Override
    public void onInterrupt() {}
}
