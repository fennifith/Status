package com.james.status.adapters;

import android.content.Context;
import android.graphics.Color;
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
import com.james.status.data.AppStatusData;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.PreferenceUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.Locale;

public class AppStatusPreviewAdapter extends RecyclerView.Adapter<AppStatusPreviewAdapter.ViewHolder> {

    private Context context;
    private ArrayList<AppStatusData> apps;
    private Gson gson;
    private List<String> jsons;

    private OnSizeChangedListener listener;

    public AppStatusPreviewAdapter(final Context context) {
        this.context = context;
        gson = new Gson();

        reload();
    }

    public AppStatusPreviewAdapter setOnSizeChangedListener(OnSizeChangedListener listener) {
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
            AppStatusData app = getApp(position);

            int color = ColorUtils.muteColor(Color.GRAY, position);
            holder.v.findViewById(R.id.color).setBackgroundColor(color);

            TextView appView = (TextView) holder.v.findViewById(R.id.app);
            if (app != null) appView.setText(app.name);
            appView.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
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

        jsons = PreferenceUtils.getStringListPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_FULLSCREEN_APPS);
        if (jsons == null) jsons = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                for (String json : jsons) {
                    AppStatusData app = gson.fromJson(json, AppStatusData.class);
                    if (app.isFullscreen) apps.add(app);
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

    @Nullable
    private AppStatusData getApp(int position) {
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
