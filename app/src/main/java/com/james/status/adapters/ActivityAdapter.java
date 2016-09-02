package com.james.status.adapters;

import android.animation.ValueAnimator;
import android.app.Dialog;
import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
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
import com.james.status.data.ActivityColorData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<ActivityColorData> apps;
    private Gson gson;
    private List<String> jsons;
    private String packageName;

    public ActivityAdapter(final Context context, final String packageName) {
        this.context = context;
        this.packageName = packageName;
        packageManager = context.getPackageManager();
        apps = new ArrayList<>();
        gson = new Gson();

        jsons = PreferenceUtils.getStringListPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS);
        if (jsons == null) jsons = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                final List<ActivityColorData> loadedApps = new ArrayList<>();

                PackageInfo packageInfo;
                try {
                    packageInfo = packageManager.getPackageInfo(packageName, PackageManager.GET_ACTIVITIES);
                } catch (PackageManager.NameNotFoundException e) {
                    return;
                }

                if (packageInfo.activities != null) {
                    for (ActivityInfo activity : packageInfo.activities) {
                        loadedApps.add(new ActivityColorData(packageManager, activity));
                    }
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(loadedApps, new Comparator<ActivityColorData>() {
                            @Override
                            public int compare(ActivityColorData lhs, ActivityColorData rhs) {
                                return lhs.label.compareToIgnoreCase(rhs.label);
                            }
                        });

                        apps = loadedApps;
                        notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    public String getPackageName() {
        return packageName;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_color, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ActivityColorData app = apps.get(position);

        for (String json : jsons) {
            ActivityColorData data = gson.fromJson(json, ActivityColorData.class);
            if (data.packageName.matches(app.packageName) && (data.name != null && app.name != null && data.name.matches(app.name)) && data.color != null) {
                app.color = data.color;
                break;
            }
        }

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Thread() {
            @Override
            public void run() {
                ActivityColorData app = getApp(holder.getAdapterPosition());
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

        TextView appName = (TextView) holder.v.findViewById(R.id.app);
        if (app.label != null && app.label.length() > 0) {
            appName.setVisibility(View.VISIBLE);
            appName.setText(app.label);
        } else appName.setVisibility(View.GONE);

        ((TextView) holder.v.findViewById(R.id.appPackage)).setText(app.getComponentName().getClassName());

        new Thread() {
            @Override
            public void run() {
                ActivityColorData app = getApp(holder.getAdapterPosition());
                if (app == null) return;
                if (app.cachedColor == null)
                    app.cachedColor = ColorUtils.getPrimaryColor(context, app.getComponentName());

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        ActivityColorData app = getApp(holder.getAdapterPosition());
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
                        } else
                            ((CustomImageView) holder.v.findViewById(R.id.color)).transition(new ColorDrawable(color));
                    }
                });
            }
        }.start();

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                ActivityColorData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                Dialog dialog = new ColorPickerDialog(context).setPreference(app.color).setDefaultPreference(getDefaultColor(app)).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                    @Override
                    public void onPreference(PreferenceDialog dialog, Integer color) {
                        ActivityColorData app = getApp(holder.getAdapterPosition());
                        if (app != null) {
                            app.color = color;
                            overwrite(app);
                        }
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

    @Nullable
    private ActivityColorData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    @ColorInt
    private int getDefaultColor(ActivityColorData app) {
        if (app.cachedColor != null) return app.cachedColor;
        else {
            Integer color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (color == null) color = Color.BLACK;
            return color;
        }
    }

    private void overwrite(@NonNull ActivityColorData app) {
        List<String> jsons = new ArrayList<>();
        for (String json : this.jsons) {
            ActivityColorData data = gson.fromJson(json, ActivityColorData.class);
            if (!data.packageName.matches(app.packageName) || !(data.name.matches(app.name))) {
                jsons.add(json);
            }
        }

        jsons.add(gson.toJson(app));

        PreferenceUtils.putPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS, jsons);
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
