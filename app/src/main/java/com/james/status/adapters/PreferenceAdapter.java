package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.james.status.data.AppsColorPreferenceData;
import com.james.status.data.BooleanPreferenceData;
import com.james.status.data.ColorPreferenceData;
import com.james.status.data.ItemData;

import java.util.ArrayList;

public class PreferenceAdapter extends RecyclerView.Adapter<ItemData.ViewHolder> {

    Context context;
    ArrayList<ItemData> datas;

    public PreferenceAdapter(Context context, ArrayList<ItemData> datas) {
        this.context = context;
        this.datas = datas;
    }

    @Override
    public ItemData.ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        switch (viewType) {
            case 0:
                return BooleanPreferenceData.getViewHolder(context);
            case 1:
                return ColorPreferenceData.getViewHolder(context);
            case 2:
                return AppsColorPreferenceData.getViewHolder(context);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(ItemData.ViewHolder holder, int position) {
        datas.get(position).onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.get(position) instanceof BooleanPreferenceData) return 0;
        else if (datas.get(position) instanceof ColorPreferenceData) return 1;
        else if (datas.get(position) instanceof AppsColorPreferenceData) return 2;
        else return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}
