package com.james.status.data.preference;

import android.content.Context;
import android.view.View;

import com.james.status.data.AppPreferenceData;
import com.james.status.dialogs.preference.AppNotificationsPreferenceDialog;

import java.util.List;

public class AppNotificationsPreferenceData extends BasePreferenceData {

    private List<AppPreferenceData> apps;

    public AppNotificationsPreferenceData(Context context, Identifier identifier) {
        super(context, identifier, null);
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        super.onBindViewHolder(holder, position);
    }

    @Override
    public void onClick(View v) {
        new AppNotificationsPreferenceDialog(this).show();
    }

    public void setApps(List<AppPreferenceData> apps) {
        this.apps = apps;
    }

    /**
     * Contrary to what one might believe, this does not in fact return the selected apps
     * in this preference, but instead returns ALL of the apps on the user's phone. This
     * is used to cache the data so that the user doesn't see any more loading bars than
     * necessary. This should be done better, I know. It is a planned improvement.
     *
     * @return the apps
     */
    public List<AppPreferenceData> getApps() {
        return apps;
    }
}
