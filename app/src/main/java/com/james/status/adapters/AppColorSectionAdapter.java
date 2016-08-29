package com.james.status.adapters;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.ActivityColorData;
import com.james.status.utils.ImageUtils;
import com.james.status.views.CustomImageView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;

public class AppColorSectionAdapter extends RecyclerView.Adapter<AppColorSectionAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private ArrayList<ActivityColorData> apps;

    public AppColorSectionAdapter(final Context context) {
        this.context = context;
        packageManager = context.getPackageManager();
        apps = new ArrayList<>();

        new Thread() {
            @Override
            public void run() {
                final ArrayList<ActivityColorData> loadedApps = new ArrayList<>();

                for (ResolveInfo info : packageManager.queryIntentActivities(new Intent(Intent.ACTION_MAIN, null).addCategory(Intent.CATEGORY_LAUNCHER), 0)) {
                    loadedApps.add(new ActivityColorData(packageManager, info.activityInfo.applicationInfo));
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

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_section, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        ActivityColorData app = apps.get(position);

        TextView appName = (TextView) holder.v.findViewById(R.id.app);
        appName.setText(app.label);

        holder.v.findViewById(R.id.recycler).setVisibility(View.GONE);
        ImageUtils.tintDrawable((CustomImageView) holder.v.findViewById(R.id.expand), ImageUtils.getVectorDrawable(context, R.drawable.ic_expand), Color.BLACK);

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                RecyclerView recycler = (RecyclerView) holder.v.findViewById(R.id.recycler);
                CustomImageView imageView = (CustomImageView) holder.v.findViewById(R.id.expand);

                ActivityColorData activity = getApp(holder.getAdapterPosition());
                if (activity == null) {
                    recycler.setVisibility(View.GONE);
                    ImageUtils.tintDrawable(imageView, ImageUtils.getVectorDrawable(context, R.drawable.ic_expand), Color.BLACK);
                    return;
                }

                if (recycler.getAdapter() == null || !(recycler.getAdapter() instanceof AppColorAdapter) || !((AppColorAdapter) recycler.getAdapter()).getPackageName().matches(activity.packageName)) {
                    recycler.setLayoutManager(new GridLayoutManager(context, 1));
                    recycler.setNestedScrollingEnabled(false);
                    recycler.setAdapter(new AppColorAdapter(context, apps.get(holder.getAdapterPosition()).packageName));
                }

                if (recycler.getVisibility() == View.GONE) {
                    recycler.setVisibility(View.VISIBLE);
                    ImageUtils.tintDrawable(imageView, ImageUtils.getVectorDrawable(context, R.drawable.ic_collapse), Color.BLACK);
                } else {
                    recycler.setVisibility(View.GONE);
                    ImageUtils.tintDrawable(imageView, ImageUtils.getVectorDrawable(context, R.drawable.ic_expand), Color.BLACK);
                }
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
