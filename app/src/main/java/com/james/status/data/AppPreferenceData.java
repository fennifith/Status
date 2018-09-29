package com.james.status.data;

import android.content.ComponentName;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;

import com.james.status.R;
import com.james.status.data.preference.BasePreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;

public class AppPreferenceData {

    private String componentName;
    private String label;

    private List<AppPreferenceData> activities;

    public AppPreferenceData(Context context, String componentName) {
        this.componentName = componentName;
    }

    public String getComponentName() {
        return componentName;
    }

    public String getPackageName() {
        return componentName.split("/")[0];
    }

    @Nullable
    public String getName() {
        String[] arr = componentName.split("/");
        if (arr.length > 0)
            return arr[arr.length - 1];
        else return null;
    }

    @Nullable
    public List<AppPreferenceData> getActivities() {
        return activities;
    }

    @Nullable
    public String getActivityName() {
        String[] arr = componentName.split("/");
        if (arr.length > 1)
            return arr[1];
        else return null;
    }

    public boolean isActivity() {
        return componentName.contains("/");
    }

    @Nullable
    public String getLabel(Context context) {
        if (label != null)
            return label;

        PackageManager manager = context.getPackageManager();
        String activityName = getActivityName();
        if (activityName != null) {
            try {
                ActivityInfo info = manager.getActivityInfo(new ComponentName(getPackageName(), activityName), 0);
                label = info.loadLabel(manager).toString();
                return label;
            } catch (PackageManager.NameNotFoundException ignored) {
            }
        }

        try {
            ApplicationInfo info = manager.getApplicationInfo(getPackageName(), 0);
            label = manager.getApplicationLabel(info).toString();
        } catch (PackageManager.NameNotFoundException ignored) {
        }

        return label;
    }

    @Nullable
    public Integer getColor(Context context) {
        return PreferenceData.APP_COLOR.getSpecificOverriddenValue(context, (Integer) PreferenceData.APP_COLOR.getSpecificOverriddenValue(context, null, getPackageName()), getIdentifierArgs());
    }

    public boolean isFullScreen(Context context) {
        return PreferenceData.APP_FULLSCREEN.getSpecificOverriddenValue(context, (Boolean) PreferenceData.APP_FULLSCREEN.getSpecificValue(context, getPackageName()), getIdentifierArgs());
    }

    public boolean isFullScreenIgnore(Context context) {
        return PreferenceData.APP_FULLSCREEN_IGNORE.getSpecificOverriddenValue(context, (Boolean) PreferenceData.APP_FULLSCREEN_IGNORE.getSpecificValue(context, getPackageName()), getIdentifierArgs());
    }

    @Nullable
    public Integer getColorCache(Context context, Integer version) {
        if (version.equals(PreferenceData.APP_COLOR_CACHE_VERSION.getSpecificOverriddenValue(context, null, getIdentifierArgs())))
            return PreferenceData.APP_COLOR_CACHE.getSpecificOverriddenValue(context, null, getIdentifierArgs());
        else return null;
    }

    public void setColorCache(Context context, int version, int color) {
        PreferenceData.APP_COLOR_CACHE.setValue(context, color, getIdentifierArgs());
        PreferenceData.APP_COLOR_CACHE_VERSION.setValue(context, version, getIdentifierArgs());
    }

    public void setActivities(List<AppPreferenceData> activities) {
        if (activities != null)
            this.activities = activities;
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
