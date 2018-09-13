package com.james.status.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

public class AppPreferenceData {

    private String componentName;
    private String label;

    private Integer color;
    private boolean isFullScreen;
    private boolean isFullScreenIgnore;

    public AppPreferenceData(Context context, String componentName) {
        this.componentName = componentName;
        color = PreferenceData.APP_COLOR.getSpecificOverriddenValue(context, null, componentName);
        isFullScreen = PreferenceData.APP_FULLSCREEN.getSpecificValue(context, componentName);
        isFullScreen = PreferenceData.APP_FULLSCREEN_IGNORE.getSpecificValue(context, componentName);
    }

    public String getComponentName() {
        return componentName;
    }

    public String getPackageName() {
        return componentName.split("/")[0];
    }

    @Nullable
    public String getLabel(Context context) {
        if (label != null)
            return label;

        PackageManager manager = context.getPackageManager();
        try {
            ApplicationInfo info = manager.getApplicationInfo(getPackageName(), 0);
            label = manager.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return label;
    }

    @Nullable
    public Integer getColor() {
        return color;
    }

    public boolean isFullScreen() {
        return isFullScreen;
    }

    public boolean isFullScreenIgnore() {
        return isFullScreenIgnore;
    }

    public void setColor(Context context, @Nullable Integer color) {
        PreferenceData.APP_COLOR.setValue(context, color, componentName);
        this.color = color;
    }

    public void setFullScreen(Context context, boolean isFullScreen) {
        PreferenceData.APP_FULLSCREEN.setValue(context, isFullScreen);
        this.isFullScreen = isFullScreen;
    }

    public void setFullScreenIgnore(Context context, boolean isFullScreenIgnore) {
        PreferenceData.APP_FULLSCREEN_IGNORE.setValue(context, isFullScreenIgnore);
        this.isFullScreenIgnore = isFullScreenIgnore;
    }

}
