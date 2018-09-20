package com.james.status.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.james.status.R;
import com.james.status.data.AppPreferenceData;
import com.james.status.dialogs.preference.AppPreferenceDialog;
import com.james.status.utils.StringUtils;
import com.james.status.views.CustomImageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<AppPreferenceData> apps;

    public AppAdapter(Context context, List<AppPreferenceData> apps) {
        this.context = context;
        packageManager = context.getPackageManager();

        this.apps = apps;
        Collections.sort(apps, new Comparator<AppPreferenceData>() {
            @Override
            public int compare(AppPreferenceData lhs, AppPreferenceData rhs) {
                return lhs.getLabel(AppAdapter.this.context).compareToIgnoreCase(rhs.getLabel(AppAdapter.this.context));
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppPreferenceData app = getApp(position);
        if (app == null) return;

        holder.name.setText(app.getLabel(holder.name.getContext()));
        holder.packageName.setText(app.getComponentName());

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
                AppPreferenceData app = getApp(holder.getAdapterPosition());
                if (app != null)
                    new AppPreferenceDialog(context, app).show();
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
                    return lhs.getLabel(context).compareToIgnoreCase(rhs.getLabel(context));
                }
            });
        } else {
            Collections.sort(apps, new Comparator<AppPreferenceData>() {
                @Override
                public int compare(AppPreferenceData lhs, AppPreferenceData rhs) {
                    int value = 0;

                    value += StringUtils.difference(lhs.getLabel(context).toLowerCase(), string).length();
                    value += StringUtils.difference(lhs.getComponentName(), string).length();
                    value -= StringUtils.difference(rhs.getLabel(context).toLowerCase(), string).length();
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
        View more;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            name = v.findViewById(R.id.appName);
            packageName = v.findViewById(R.id.appPackage);
            icon = v.findViewById(R.id.icon);
            more = v.findViewById(R.id.more);
        }
    }
}
