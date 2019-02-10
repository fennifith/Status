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

package com.james.status.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.james.status.R;
import com.james.status.data.AppPreferenceData;
import com.james.status.data.PreferenceData;
import com.james.status.utils.StringUtils;
import com.james.status.views.CustomImageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.SwitchCompat;
import androidx.recyclerview.widget.RecyclerView;

public class AppNotificationsAdapter extends RecyclerView.Adapter<AppNotificationsAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<AppPreferenceData> apps;

    public AppNotificationsAdapter(Context context, List<AppPreferenceData> apps) {
        this.context = context;
        packageManager = context.getPackageManager();

        this.apps = apps;
        Collections.sort(apps, new Comparator<AppPreferenceData>() {
            @Override
            public int compare(AppPreferenceData lhs, AppPreferenceData rhs) {
                String label1 = lhs.getLabel(AppNotificationsAdapter.this.context);
                String label2 = rhs.getLabel(AppNotificationsAdapter.this.context);
                if (label1 != null && label2 != null)
                    return label1.compareToIgnoreCase(label2);
                else return 0;
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_switch, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppPreferenceData app = getApp(position);
        if (app == null) return;

        holder.name.setText(app.getLabel(context));
        holder.packageName.setText(app.getName());

        holder.icon.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Action<Drawable>() {
            @NonNull
            @Override
            public String id() {
                return "appIcon";
            }

            @Nullable
            @Override
            protected Drawable run() throws InterruptedException {
                AppPreferenceData app = getApp(holder.getAdapterPosition());
                if (app != null) {
                    try {
                        return packageManager.getApplicationIcon(app.getPackageName());
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                }

                return null;
            }

            @Override
            protected void done(@Nullable Drawable result) {
                if (result != null)
                    holder.icon.setImageDrawable(result);
            }
        }.execute();

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                holder.enabledSwitch.toggle();
            }
        });

        holder.enabledSwitch.setOnCheckedChangeListener(null);
        holder.enabledSwitch.setChecked((boolean) PreferenceData.APP_NOTIFICATIONS.getSpecificValue(context, apps.get(position).getPackageName()));
        holder.enabledSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppPreferenceData app = getApp(holder.getAdapterPosition());
                if (app != null)
                    PreferenceData.APP_NOTIFICATIONS.setValue(context, isChecked, app.getPackageName());
            }
        });
    }

    @Nullable
    private AppPreferenceData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void filter(@Nullable final String string) {
        if (string == null || string.length() < 1) {
            Collections.sort(apps, new Comparator<AppPreferenceData>() {
                @Override
                public int compare(AppPreferenceData lhs, AppPreferenceData rhs) {
                    String label1 = lhs.getLabel(AppNotificationsAdapter.this.context);
                    String label2 = rhs.getLabel(AppNotificationsAdapter.this.context);
                    if (label1 != null && label2 != null)
                        return label1.compareToIgnoreCase(label2);
                    else return 0;
                }
            });
        } else {
            Collections.sort(apps, new Comparator<AppPreferenceData>() {
                @Override
                public int compare(AppPreferenceData lhs, AppPreferenceData rhs) {
                    int value = 0;

                    String label1 = lhs.getLabel(AppNotificationsAdapter.this.context);
                    String label2 = rhs.getLabel(AppNotificationsAdapter.this.context);
                    if (label1 != null && label2 != null) {
                        value += StringUtils.difference(label1.toLowerCase(), string).length();
                        value -= StringUtils.difference(label2.toLowerCase(), string).length();
                    }

                    value += StringUtils.difference(lhs.getComponentName(), string).length();
                    value -= StringUtils.difference(rhs.getComponentName(), string).length();

                    return value;
                }
            });
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView name, packageName;
        CustomImageView icon;
        SwitchCompat enabledSwitch;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            name = v.findViewById(R.id.appName);
            packageName = v.findViewById(R.id.appPackage);
            icon = v.findViewById(R.id.icon);
            enabledSwitch = v.findViewById(R.id.enabledSwitch);
        }
    }
}
