package com.james.status.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.AppData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Set;

public class AppColorPreviewAdapter extends RecyclerView.Adapter<AppColorPreviewAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private ArrayList<AppData> apps;
    private Gson gson;
    private Set<String> jsons;

    private OnSizeChangedListener listener;

    public AppColorPreviewAdapter(final Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        gson = new Gson();

        reload();
    }

    public AppColorPreviewAdapter setOnSizeChangedListener(OnSizeChangedListener listener) {
        this.listener = listener;
        return this;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app, null));
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

        TextView appView = (TextView) holder.v.findViewById(R.id.app);
        appView.setText(app.name);
        appView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondaryInverse));

        ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(ColorUtils.muteColor(Color.DKGRAY, position)));

        new Thread() {
            @Override
            public void run() {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                final int color = ColorUtils.getStatusBarColor(context, packageManager, app.packageName);

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
                            ValueAnimator animator = ValueAnimator.ofArgb(Color.GRAY, ColorUtils.muteColor(color, holder.getAdapterPosition()));
                            animator.setDuration(150);
                            animator.addUpdateListener(new ValueAnimator.AnimatorUpdateListener() {
                                @Override
                                public void onAnimationUpdate(ValueAnimator valueAnimator) {
                                    int color = (int) valueAnimator.getAnimatedValue();
                                    ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(color));
                                    ((TextView) holder.v.findViewById(R.id.app)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                                }
                            });
                            animator.start();
                        } else {
                            ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(color));
                            ((TextView) holder.v.findViewById(R.id.app)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                        }
                    }
                });
            }
        }.start();
    }

    @Override
    public int getItemCount() {
        int size = apps.size();
        if (listener != null) listener.onSizeChanged(size);
        return size;
    }

    public void reload() {
        apps = new ArrayList<>();

        jsons = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR_APPS);
        if (jsons == null) jsons = new HashSet<>();

        new Thread() {
            @Override
            public void run() {
                for (String json : jsons) {
                    AppData app = gson.fromJson(json, AppData.class);
                    if (app.color != null) apps.add(app);
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

    @Nullable
    private AppData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    public interface OnSizeChangedListener {
        void onSizeChanged(int size);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
