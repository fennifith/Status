package com.james.status.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.data.AppData;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private List<AppData.ActivityData> activities;

    public ActivityAdapter(Context context, List<AppData.ActivityData> activites) {
        this.context = context;
        this.activities = activites;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_color, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {

    }

    @Nullable
    private AppData.ActivityData getActivity(int position) {
        if (position < 0 || position >= activities.size()) return null;
        else return activities.get(position);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
