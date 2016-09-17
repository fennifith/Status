package com.james.status.adapters;

import android.content.Context;
import android.content.Intent;
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
import com.james.status.utils.ImageUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<AppData> originalApps, apps;

    public AppAdapter(final Context context, List<AppData> apps) {
        this.context = context;
        packageManager = context.getPackageManager();

        originalApps = new ArrayList<>();
        originalApps.addAll(apps);

        Collections.sort(originalApps, new Comparator<AppData>() {
            @Override
            public int compare(AppData lhs, AppData rhs) {
                return lhs.label.compareToIgnoreCase(rhs.label);
            }
        });

        this.apps = new ArrayList<>();
        this.apps.addAll(originalApps);
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

                        final int color = app.getColor(context), defaultColor = app.getDefaultColor(context);

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                AppData app = getApp(holder.getAdapterPosition());
                                if (app == null) return;

                                PreferenceDialog dialog = new ColorPickerDialog(context).setTag(app).setPreference(color).setDefaultPreference(defaultColor).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                                    @Override
                                    public void onPreference(PreferenceDialog dialog, Integer preference) {
                                        Object tag = dialog.getTag();
                                        if (tag != null && tag instanceof AppData)
                                            ((AppData) tag).putPreference(context, AppData.PreferenceIdentifier.COLOR, preference);

                                        notifyItemChanged(holder.getAdapterPosition());
                                    }

                                    @Override
                                    public void onCancel(PreferenceDialog dialog) {
                                    }
                                });

                                dialog.setTitle(app.label);
                                dialog.show();
                            }
                        });
                    }
                }.start();
            }
        });

        new Thread() {
            @Override
            public void run() {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                final int color = app.getColor(context);

                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(color));

                        holder.v.findViewById(R.id.titleBar).setBackgroundColor(color);
                        ((TextView) holder.v.findViewById(R.id.appName)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
                        ((TextView) holder.v.findViewById(R.id.appPackage)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                        ImageUtils.tintDrawable(((CustomImageView) holder.v.findViewById(R.id.launchIcon)), ImageUtils.getVectorDrawable(context, R.drawable.ic_launch), ColorUtils.isColorDark(color) ? Color.WHITE : Color.BLACK);
                    }
                });
            }
        }.start();

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

                StaticUtils.updateStatusService(context);
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
        apps.clear();

        if (string == null || string.length() < 1) {
            apps.addAll(originalApps);
        } else {
            string = string.toLowerCase();

            for (AppData app : originalApps) {
                if ((app.label != null && (app.label.toLowerCase().contains(string) || string.contains(app.label.toLowerCase()))) || (app.name != null && (app.name.toLowerCase().contains(string) || string.contains(app.name.toLowerCase()))))
                    apps.add(app);
            }
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
