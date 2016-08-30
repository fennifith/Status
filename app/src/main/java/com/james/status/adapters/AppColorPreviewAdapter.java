package com.james.status.adapters;

import android.animation.ValueAnimator;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.os.Handler;
import android.support.annotation.ColorInt;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.google.gson.Gson;
import com.james.status.R;
import com.james.status.data.ActivityColorData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;

public class AppColorPreviewAdapter extends RecyclerView.Adapter<AppColorPreviewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<ActivityColorData> apps;
    private Gson gson;
    private Set<String> jsons;

    private OnSizeChangedListener listener;

    public AppColorPreviewAdapter(final Context context) {
        this.context = context;
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
        if (position < 4) {
            ActivityColorData app = apps.get(position);

            for (String json : jsons) {
                ActivityColorData data = gson.fromJson(json, ActivityColorData.class);
                if (data.packageName.matches(app.packageName) && data.name.matches(app.name) && data.color != null) {
                    app.color = data.color;
                    break;
                }
            }

            String[] packages = app.getComponentName().getClassName().split(".");

            TextView appView = (TextView) holder.v.findViewById(R.id.app);
            if (packages.length > 0) appView.setText(String.format("%s - %s", app.label, packages[packages.length - 1]));
            else appView.setText(app.label);

            appView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondaryInverse));

            ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(new ColorDrawable(ColorUtils.muteColor(Color.DKGRAY, position)));

            int color = app.color != null ? app.color : getDefaultColor();

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
        } else {
            TextView appView = (TextView) holder.v.findViewById(R.id.app);
            appView.setText(String.format(Locale.getDefault(), "+%d MORE", apps.size() - 4));
            appView.setTextColor(ContextCompat.getColor(context, R.color.textColorSecondary));

            ((CustomImageView) holder.v.findViewById(R.id.color)).setImageDrawable(null);
        }
    }

    @Override
    public int getItemCount() {
        int size = apps.size();
        if (listener != null) listener.onSizeChanged(size);
        return Math.min(size, 5);
    }

    public void reload() {
        apps = new ArrayList<>();

        jsons = PreferenceUtils.getStringSetPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLORED_APPS);
        if (jsons == null) jsons = new HashSet<>();

        new Thread() {
            @Override
            public void run() {
                for (String json : jsons) {
                    ActivityColorData app = gson.fromJson(json, ActivityColorData.class);
                    if (app.color != null) apps.add(app);
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        Collections.sort(apps, new Comparator<ActivityColorData>() {
                            @Override
                            public int compare(ActivityColorData lhs, ActivityColorData rhs) {
                                return lhs.label.compareToIgnoreCase(rhs.label);
                            }
                        });

                        notifyDataSetChanged();
                    }
                });
            }
        }.start();
    }

    @ColorInt
    private int getDefaultColor() {
        Integer color = PreferenceUtils.getIntegerPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_COLOR);
        if (color == null) color = Color.BLACK;
        return color;
    }

    @Nullable
    private ActivityColorData getApp(int position) {
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
