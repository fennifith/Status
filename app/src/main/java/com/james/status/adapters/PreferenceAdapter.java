package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.ViewGroup;

import com.james.status.data.preference.AppsColorPreferenceData;
import com.james.status.data.preference.BooleanPreferenceData;
import com.james.status.data.preference.ColorPreferenceData;
import com.james.status.data.preference.IconPreferenceData;
import com.james.status.data.preference.IntegerPreferenceData;
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
        switch (viewType) {
            case 0:
                return BooleanPreferenceData.getViewHolder(context);
            case 1:
                return ColorPreferenceData.getViewHolder(context);
            case 2:
                return AppsColorPreferenceData.getViewHolder(context);
            case 3:
                return IconPreferenceData.getViewHolder(context);
            case 4:
                return IntegerPreferenceData.getViewHolder(context);
            default:
                return null;
        }
    }

    @Override
    public void onBindViewHolder(PreferenceData.ViewHolder holder, int position) {
        datas.get(position).onBindViewHolder(holder, position);
    }

    @Override
    public int getItemViewType(int position) {
        if (datas.get(position) instanceof BooleanPreferenceData) return 0;
        else if (datas.get(position) instanceof ColorPreferenceData) return 1;
        else if (datas.get(position) instanceof AppsColorPreferenceData) return 2;
        else if (datas.get(position) instanceof IconPreferenceData) return 3;
        else if (datas.get(position) instanceof IntegerPreferenceData) return 4;
        else return super.getItemViewType(position);
    }

    @Override
    public int getItemCount() {
        return datas.size();
    }
}
