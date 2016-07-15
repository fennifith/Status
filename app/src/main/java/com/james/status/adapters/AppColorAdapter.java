package com.james.status.adapters;

import android.animation.ValueAnimator;
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
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.AppData;
import com.james.status.dialogs.ColorPickerDialog;
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
    private ArrayList<AppData> apps;
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
                for (ResolveInfo info : packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                    apps.add(new AppData(packageManager, info));
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(apps, new Comparator<AppData>() {
                            @Override
                            public int compare(AppData lhs, AppData rhs) {
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
        AppData app = apps.get(position);

        for (String json : jsons) {
            AppData data = gson.fromJson(json, AppData.class);
            if (data.packageName.matches(app.packageName) && data.color != null) {
                app.color = data.color;
                break;
            }
        }

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
                            ((CustomImageView) holder.v.findViewById(R.id.icon)).transition(icon);
                    }
                });
            }
        }.start();

        SwitchCompat appSwitch = (SwitchCompat) holder.v.findViewById(R.id.app);
        appSwitch.setText(app.name);
        appSwitch.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondaryInverse));
        appSwitch.setOnCheckedChangeListener(null); //totally not a spaghetti way of preventing an exception from being thrown...
        appSwitch.setChecked(app.color != null);

        ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(ColorUtils.muteColor(Color.DKGRAY, position)));

        new Thread() {
            @Override
            public void run() {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;
                if (app.cachedColor == null)
                    app.cachedColor = ColorUtils.getStatusBarColor(context, packageManager, app.packageName);

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        AppData app = getApp(holder.getAdapterPosition());
                        if (app == null) return;

                        int color = ColorUtils.muteColor(app.color != null ? app.color : getDefaultColor(app), holder.getAdapterPosition());

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

                            ValueAnimator textAnimator = ValueAnimator.ofArgb(((SwitchCompat) holder.v.findViewById(R.id.app)).getCurrentTextColor(), ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                            textAnimator.setDuration(150);
                            textAnimator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    ((SwitchCompat) holder.v.findViewById(R.id.app)).setTextColor((int) valueAnimator.getAnimatedValue());
                                }
                            });
                            textAnimator.start();
                        } else {
                            ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(color));
                            ((SwitchCompat) holder.v.findViewById(R.id.app)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                        }
                    }
                });
            }
        }.start();

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                new ColorPickerDialog(context).setColor(app.color != null ? app.color : getDefaultColor(app)).setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                    @Override
                    public void onColorPicked(int color) {
                        AppData app = getApp(holder.getAdapterPosition());
                        if (app != null) {
                            app.color = color;
                            overwrite(app);
                        }
                    }

                    @Override
                    public void onCancel() {
                    }
                }).show();
            }
        });

        appSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton compoundButton, boolean b) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                if (b) {
                    new ColorPickerDialog(context).setColor(app.color != null ? app.color : getDefaultColor(app)).setOnColorPickedListener(new ColorPickerDialog.OnColorPickedListener() {
                        @Override
                        public void onColorPicked(int color) {
                            AppData app = getApp(holder.getAdapterPosition());
                            if (app != null) {
                                app.color = color;
                                overwrite(app);
                            }
                        }

                        @Override
                        public void onCancel() {
                            ((SwitchCompat) holder.v.findViewById(R.id.app)).setChecked(false);
                        }
                    }).show();
                } else {
                    app.color = null;
                    overwrite(app);
                }
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

    @ColorInt
    private int getDefaultColor(AppData app) {
        if (app.cachedColor != null) return app.cachedColor;
        else {
            Integer color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
            if (color == null) color = Color.BLACK;
            return color;
        }
    }

    private void overwrite(@NonNull AppData app) {
        Set<String> jsons = new HashSet<>();
        for (String json : this.jsons) {
            AppData data = gson.fromJson(json, AppData.class);
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
