package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import com.james.status.data.preference.PreferenceData;

import java.util.ArrayList;

public class PreferenceAdapter extends RecyclerView.Adapter<PreferenceData.ViewHolder> {

    Context context;
    ArrayList<PreferenceData> datas;

    public PreferenceAdapter(Context context, ArrayList<PreferenceData> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public PreferenceData.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        if (viewType < 0 || viewType >= datas.size()) return null;
        return datas.get(viewType).getViewHolder(LayoutInflater.from(context), parent);
    }

    @Override
    public void onBindViewHolder(PreferenceData.ViewHolder holder, int position) {
        datas.get(position).onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        PreferenceData data = datas.get(position);
        for (int i = 0; i < datas.size(); i++) {
            if (datas.get(i).getClass().equals(data.getClass())) return i;
        }

        return -1;
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}
