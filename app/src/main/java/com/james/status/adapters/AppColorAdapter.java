package com.james.status.adapters;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.AppColorData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class AppColorAdapter extends RecyclerView.Adapter<AppColorAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private ArrayList<AppColorData> apps;
    private Gson gson;
    private Set<String> jsons;

    public AppColorAdapter(final Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        apps = new ArrayList<>();
        gson = new Gson();

        jsons = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_APPS);
        if (jsons == null) jsons = new HashSet<>();

        new Thread() {
            @Override
            public void run() {
                final ArrayList<AppColorData> loadedApps = new ArrayList<>();

                for (ResolveInfo info : packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                    loadedApps.add(new AppColorData(packageManager, info));
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(loadedApps, new Comparator<AppColorData>() {
                            @Override
                            public int compare(AppColorData lhs, AppColorData rhs) {
                                return lhs.name.compareToIgnoreCase(rhs.name);
                            }
                        });

                        apps = loadedApps;
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
        AppColorData app = apps.get(position);

        for (String json : jsons) {
            AppColorData data = gson.fromJson(json, AppColorData.class);
            if (data.packageName.matches(app.packageName) && data.color != null) {
                app.color = data.color;
                break;
            }
        }

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Thread() {
            @Override
            public void run() {
                AppColorData app = getApp(holder.getAdapterPosition());
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

        TextView appName = (TextView) holder.v.findViewById(R.id.app);
        appName.setText(app.name);

        new Thread() {
            @Override
            public void run() {
                AppColorData app = getApp(holder.getAdapterPosition());
                if (app == null) return;
                if (app.cachedColor == null)
                    app.cachedColor = ColorUtils.getStatusBarColor(context, packageManager, app.packageName);

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        AppColorData app = getApp(holder.getAdapterPosition());
                        if (app == null) return;

                        int color = app.color != null ? app.color : getDefaultColor(app);

                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ValueAnimator animator = ValueAnimator.ofArgb(ColorUtils.muteColor(Color.DKGRAY, holder.getAdapterPosition()), color);
                            animator.setDuration(150);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable((int) valueAnimator.getAnimatedValue()));
                                }
                            });
                            animator.start();
                        } else {
                            ((CustomImageView) holder.v.findViewById(R.id.color)).transition(new ColorDrawable(color));
                        }
                    }
                });
            }
        }.start();

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppColorData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                Dialog dialog = new ColorPickerDialog(context).setPreference(app.color != null ? app.color : getDefaultColor(app)).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                    @Override
                    public void onPreference(Integer color) {
                        AppColorData app = getApp(holder.getAdapterPosition());
                        if (app != null) {
                            app.color = color;
                            overwrite(app);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                });

                dialog.setTitle(app.name);

                dialog.show();
            }
        });
    }

    @Nullable
    private AppColorData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    @ColorInt
    private int getDefaultColor(AppColorData app) {
        if (app.cachedColor != null) return app.cachedColor;
        else {
            Integer color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (color == null) color = Color.BLACK;
            return color;
        }
    }

    private void overwrite(@NonNull AppColorData app) {
        Set<String> jsons = new HashSet<>();
        for (String json : this.jsons) {
            AppColorData data = gson.fromJson(json, AppColorData.class);
            if (!data.packageName.matches(app.packageName)) {
                jsons.add(json);
            }
        }

        jsons.add(gson.toJson(app));

        PreferenceUtils.putPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_APPS, jsons);
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
