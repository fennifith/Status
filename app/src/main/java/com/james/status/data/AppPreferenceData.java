package com.james.status.data;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.support.annotation.Nullable;

import com.james.status.R;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;

import java.util.ArrayList;
import java.util.List;

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

    public List<BasePreferenceData> getPreferences(final Context context) {
        List<BasePreferenceData> preferences = new ArrayList<>();

        preferences.add(new ColorPreferenceData(
                context,
                new BasePreferenceData.Identifier<Integer>(
                        PreferenceData.APP_COLOR,
                        context.getString(R.string.preference_status_color),
                        null,
                        getIdentifierArgs()
                ),
                new BasePreferenceData.OnPreferenceChangeListener<Integer>() {
                    @Override
                    public void onPreferenceChange(Integer preference) {

                    }
                }
        ).withAlpha(new BasePreferenceData.ValueGetter<Boolean>() {
            @Override
            public Boolean get() {
                return PreferenceData.STATUS_TRANSPARENT_MODE.getValue(context);
            }
        }).withNullable(true));

        preferences.add(new BooleanPreferenceData(
                context,
                new BasePreferenceData.Identifier<Boolean>(
                        PreferenceData.APP_FULLSCREEN,
                        context.getString(R.string.dialog_preference_fullscreen),
                        getIdentifierArgs()
                ),
                new BasePreferenceData.OnPreferenceChangeListener<Boolean>() {
                    @Override
                    public void onPreferenceChange(Boolean preference) {
                        if (preference.equals(PreferenceData.APP_FULLSCREEN.getDefaultValue()))
                            PreferenceData.APP_FULLSCREEN.setValue(context, null, getIdentifierArgs());
                    }
                }
        ));

        preferences.add(new BooleanPreferenceData(
                context,
                new BasePreferenceData.Identifier<Boolean>(
                        PreferenceData.APP_FULLSCREEN_IGNORE,
                        context.getString(R.string.dialog_preference_fullscreen_ignore),
                        getIdentifierArgs()
                ),
                new BasePreferenceData.OnPreferenceChangeListener<Boolean>() {
                    @Override
                    public void onPreferenceChange(Boolean preference) {
                        if (preference.equals(PreferenceData.APP_FULLSCREEN_IGNORE.getDefaultValue()))
                            PreferenceData.APP_FULLSCREEN_IGNORE.setValue(context, null, getIdentifierArgs());
                    }
                }
        ));

        return preferences;
    }

    public String[] getIdentifierArgs() {
        return new String[]{componentName};
    }

}
