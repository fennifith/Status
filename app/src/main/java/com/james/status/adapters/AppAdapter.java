package com.james.status.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.activities.AppSettingActivity;
import com.james.status.data.AppData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.ColorUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<AppData> originalApps, apps;

    public AppAdapter(final Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        originalApps = new ArrayList<>();
        apps = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                final List<AppData> loadedApps = new ArrayList<>();

                for (ApplicationInfo applicationInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
                    PackageInfo packageInfo;

                    try {
                        packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_ACTIVITIES);
                    } catch (PackageManager.NameNotFoundException e) {
                        continue;
                    }

                    if (packageInfo.activities != null && packageInfo.activities.length > 0)
                        loadedApps.add(new AppData(packageManager, applicationInfo, packageInfo));
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(loadedApps, new Comparator<AppData>() {
                            @Override
                            public int compare(AppData lhs, AppData rhs) {
                                return lhs.label.compareToIgnoreCase(rhs.label);
                            }
                        });

                        originalApps = loadedApps;
                        apps = loadedApps;
                        notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppData app = getApp(position);
        if (app == null) return;

        ((TextView) holder.v.findViewById(R.id.appName)).setText(app.label);
        ((TextView) holder.v.findViewById(R.id.appPackage)).setText(app.packageName);

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Thread() {
            @Override
            public void run() {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                final Drawable icon;
                try {
                    icon = packageManager.getApplicationIcon(app.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    return;
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (icon != null)
                            ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(icon);
                    }
                });
            }
        }.start();

        holder.v.findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        AppData app = getApp(holder.getAdapterPosition());
                        if (app == null) return;

                        final Integer color = ColorUtils.getPrimaryColor(context, app.getComponentName());

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                AppData app = getApp(holder.getAdapterPosition());
                                if (app == null) return;

                                new ColorPickerDialog(context).setTag(app).setPreference(app.getIntegerPreference(context, AppData.PreferenceIdentifier.COLOR)).setDefaultPreference(color != null ? color : Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                                    @Override
                                    public void onPreference(PreferenceDialog dialog, Integer preference) {
                                        Object tag = dialog.getTag();
                                        if (tag != null && tag instanceof AppData)
                                            ((AppData) tag).putPreference(context, AppData.PreferenceIdentifier.COLOR, preference);

                                        ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(preference));
                                    }

                                    @Override
                                    public void onCancel(PreferenceDialog dialog) {
                                    }
                                }).show();
                            }
                        });
                    }
                }.start();
            }
        });

        final Integer color = app.getIntegerPreference(context, AppData.PreferenceIdentifier.COLOR);
        if (color != null) {
            ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(color));

            holder.v.findViewById(R.id.titleBar).setBackgroundColor(color);
            ((TextView) holder.v.findViewById(R.id.appName)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
            ((TextView) holder.v.findViewById(R.id.appPackage)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
        } else {
            new Thread() {
                @Override
                public void run() {
                    AppData app = getApp(holder.getAdapterPosition());
                    if (app == null) return;

                    final Integer color = ColorUtils.getPrimaryColor(context, app.getComponentName());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            int someColor = color != null ? color : Color.BLACK;

                            ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(someColor));

                            holder.v.findViewById(R.id.titleBar).setBackgroundColor(someColor);
                            ((TextView) holder.v.findViewById(R.id.appName)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(someColor) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
                            ((TextView) holder.v.findViewById(R.id.appPackage)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(someColor) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                        }
                    });
                }
            }.start();
        }

        SwitchCompat fullscreenSwitch = (SwitchCompat) holder.v.findViewById(R.id.fullscreenSwitch);
        fullscreenSwitch.setOnCheckedChangeListener(null);

        Boolean isFullscreen = app.getBooleanPreference(context, AppData.PreferenceIdentifier.FULLSCREEN);
        fullscreenSwitch.setChecked(isFullscreen != null && isFullscreen);

        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                app.putPreference(context, AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
            }
        });

        SwitchCompat notificationSwitch = (SwitchCompat) holder.v.findViewById(R.id.notificationSwitch);
        notificationSwitch.setOnCheckedChangeListener(null);

        Boolean isNotifications = app.getSpecificBooleanPreference(context, AppData.PreferenceIdentifier.NOTIFICATIONS);
        notificationSwitch.setChecked(isNotifications == null || isNotifications);

        notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                app.putSpecificPreference(context, AppData.PreferenceIdentifier.NOTIFICATIONS, isChecked);
            }
        });

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AppSettingActivity.class);
                intent.putExtra(AppSettingActivity.EXTRA_APP, getApp(holder.getAdapterPosition()));
                context.startActivity(intent);
            }
        });
    }

    @Nullable
    private AppData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void filter(@Nullable String string) {
        if (string != null) {
            List<AppData> newApps = new ArrayList<>();
            for (AppData app : originalApps) {
                if (app.name != null && app.name.toLowerCase().contains(string.toLowerCase())) {
                    newApps.add(app);
                    continue;
                }

                if (app.packageName != null && app.packageName.toLowerCase().contains(string.toLowerCase())) {
                    newApps.add(app);
                }
            }

            apps = newApps;
            notifyDataSetChanged();

        } else apps = originalApps;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
