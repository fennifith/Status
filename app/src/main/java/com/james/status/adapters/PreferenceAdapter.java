package com.james.status.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.james.status.data.PreferenceData;
import com.james.status.data.preference.BasePreferenceData;

import java.util.List;

import androidx.recyclerview.widget.RecyclerView;

public class PreferenceAdapter extends RecyclerView.Adapter<BasePreferenceData.ViewHolder> {

    private Context context;
    private List<BasePreferenceData> datas;

    public PreferenceAdapter(Context context, List<BasePreferenceData> datas) {
        this.context = context;
        this.datas = datas;
    }

    public void setItems(List<BasePreferenceData> datas) {
        this.datas = datas;
        notifyDataSetChanged();
    }

    @Override
    public BasePreferenceData.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0 || viewType >= datas.size()) return null;
        return datas.get(viewType).getViewHolder(LayoutInflater.from(context), parent);
    }

    @Override
    public void onBindViewHolder(BasePreferenceData.ViewHolder holder, int position) {
        if (datas.get(position).isVisible()) {
            holder.itemView.setFocusable(true);
            holder.itemView.setClickable(true);
            holder.itemView.setAlpha(0);
            holder.itemView.animate().alpha(1).setDuration(500).start();

            datas.get(position).onBindViewHolder(holder, position);
        } else {
            datas.get(position).onBindViewHolder(holder, position);

            holder.itemView.setFocusable(false);
            holder.itemView.setClickable(false);
            holder.itemView.setAlpha(0);
            holder.itemView.animate().alpha(0.5f).setDuration(250).start();
        }
    }

    @Override
    public int getItemViewType(int position) {
        BasePreferenceData data = datas.get(position);
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getClass().equals(data.getClass())) return i;
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }

    public boolean notifyPreferenceChanged(PreferenceData dependent) {
        boolean didTheThing = false;
        for (int i = 0; i < datas.size(); i++) {
            if (dependent.equals(datas.get(i).getVisibilityDependent())) {
                notifyItemChanged(i);
                didTheThing = true;
            }
        }

        return didTheThing;
    }
}
