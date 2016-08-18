package com.james.status.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.AppStatusData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class AppStatusAdapter extends RecyclerView.Adapter<AppStatusAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private ArrayList<AppStatusData> apps;
    private Gson gson;
    private Set<String> jsons;

    public AppStatusAdapter(final Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        apps = new ArrayList<>();
        gson = new Gson();

        jsons = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_FULLSCREEN_APPS);
        if (jsons == null) jsons = new HashSet<>();

        new Thread() {
            @Override
            public void run() {
                for (ResolveInfo info : packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                    apps.add(new AppStatusData(packageManager, info));
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(apps, new Comparator<AppStatusData>() {
                            @Override
                            public int compare(AppStatusData lhs, AppStatusData rhs) {
                                return lhs.name.compareToIgnoreCase(rhs.name);
                            }
                        });

                        notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_grid, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppStatusData app = getApp(position);

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Thread() {
            @Override
            public void run() {
                AppStatusData app = getApp(holder.getAdapterPosition());
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
                            ((CustomImageView) holder.v.findViewById(R.id.icon)).transition(icon);
                    }
                });
            }
        }.start();

        int color = ColorUtils.muteColor(Color.GRAY, position);
        holder.v.findViewById(R.id.color).setBackgroundColor(color);

        SwitchCompat titleView = (SwitchCompat) holder.v.findViewById(R.id.app);

        titleView.setText(app.name);
        titleView.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));

        titleView.setOnCheckedChangeListener(null);
        titleView.setChecked(app.isFullscreen);
        titleView.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AppStatusData app = getApp(holder.getAdapterPosition());

                if (app != null) {
                    app.isFullscreen = b;
                    overwrite(app);
                }
            }
        });
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    @Nullable
    private AppStatusData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    private void overwrite(@NonNull AppStatusData app) {
        Set<String> jsons = new HashSet<>();
        for (String json : this.jsons) {
            AppStatusData data = gson.fromJson(json, AppStatusData.class);
            if (!data.packageName.matches(app.packageName)) {
                jsons.add(json);
            }
        }

        jsons.add(gson.toJson(app));

        PreferenceUtils.putPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_FULLSCREEN_APPS, jsons);
        this.jsons = jsons;
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
