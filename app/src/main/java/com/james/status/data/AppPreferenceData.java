/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

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

    public AppPreferenceData(String componentName) {
        this.componentName = componentName;
    }

    /**
     * Obtain the component name of the preference; "package.name/package.name.Component" if
     * the preference is for a specific activity, just "package.name" if not.
     *
     * @return A string containing the entire component name of
     * the app.
     */
    public String getComponentName() {
        return componentName;
    }

    /**
     * Obtain the package name of the preference.
     *
     * @return A string containing the package name of the app.
     */
    public String getPackageName() {
        return componentName.split("/")[0];
    }

    /**
     * Get the component name of the preference. If the preference is not an activity,
     * this will be null.
     *
     * @return A string containing the component name of the app.
     */
    @Nullable
    public String getName() {
        String[] arr = componentName.split("/");
        if (arr.length > 0)
            return arr[arr.length - 1];
        else return null;
    }

    /**
     * Get all of the activities declared by the application. If the preference is not an
     * application, this will be null.
     *
     * @return A list of data classes representing activities declared
     *                              by the application.
     */
    @Nullable
    public List<AppPreferenceData> getActivities() {
        return activities;
    }

    /**
     * Get the name of the activity. If the preference is not an activity, this will be null.
     *
     * @return The name of the activity.
     */
    @Nullable
    public String getActivityName() {
        String[] arr = componentName.split("/");
        if (arr.length > 1)
            return arr[1];
        else return null;
    }

    /**
     * Determine whether the preference is for an activity.
     *
     * @return True if the preference is for a specific activity.
     */
    public boolean isActivity() {
        return componentName.contains("/");
    }

    /**
     * Obtain the label of the activity / application.
     *
     * @param context               The current app context.
     * @return The string label declared by the application or
     *                              component that this preference is for.
     */
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

    @Nullable
    public Integer getIconColor(Context context) {
        return PreferenceData.APP_ICON_COLOR.getSpecificOverriddenValue(context, (Integer) PreferenceData.APP_ICON_COLOR.getSpecificOverriddenValue(context, null, getPackageName()), getIdentifierArgs());
    }

    @Nullable
    public Integer getTextColor(Context context) {
        return PreferenceData.APP_TEXT_COLOR.getSpecificOverriddenValue(context, (Integer) PreferenceData.APP_TEXT_COLOR.getSpecificOverriddenValue(context, null, getPackageName()), getIdentifierArgs());
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
                preference -> { }
        ).withAlpha(() -> PreferenceData.STATUS_TRANSPARENT_MODE.getValue(context)).withNullable(true));

        preferences.add(new ColorPreferenceData(
                context,
                new BasePreferenceData.Identifier<Integer>(
                        PreferenceData.APP_ICON_COLOR,
                        context.getString(R.string.preference_color_icon),
                        null,
                        getIdentifierArgs()
                ),
                preference -> { }
        ).withAlpha(() -> PreferenceData.STATUS_TRANSPARENT_MODE.getValue(context)).withNullable(true));

        preferences.add(new ColorPreferenceData(
                context,
                new BasePreferenceData.Identifier<Integer>(
                        PreferenceData.APP_TEXT_COLOR,
                        context.getString(R.string.preference_color_text),
                        null,
                        getIdentifierArgs()
                ),
                preference -> { }
        ).withAlpha(() -> PreferenceData.STATUS_TRANSPARENT_MODE.getValue(context)).withNullable(true));

        preferences.add(new BooleanPreferenceData(
                context,
                new BasePreferenceData.Identifier<Boolean>(
                        PreferenceData.APP_FULLSCREEN,
                        context.getString(R.string.dialog_preference_fullscreen),
                        getIdentifierArgs()
                ),
                preference -> {
                    if (preference.equals(PreferenceData.APP_FULLSCREEN.getDefaultValue()))
                        PreferenceData.APP_FULLSCREEN.setValue(context, null, getIdentifierArgs());
                }
        ));

        preferences.add(new BooleanPreferenceData(
                context,
                new BasePreferenceData.Identifier<Boolean>(
                        PreferenceData.APP_FULLSCREEN_IGNORE,
                        context.getString(R.string.dialog_preference_fullscreen_ignore),
                        getIdentifierArgs()
                ),
                preference -> {
                    if (preference.equals(PreferenceData.APP_FULLSCREEN_IGNORE.getDefaultValue()))
                        PreferenceData.APP_FULLSCREEN_IGNORE.setValue(context, null, getIdentifierArgs());
                }
        ));

        return preferences;
    }

    public String[] getIdentifierArgs() {
        return new String[]{componentName};
    }

}
