package com.james.status.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v4.view.ViewCompat;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class PreferenceSectionAdapter extends RecyclerView.Adapter<PreferenceSectionAdapter.ViewHolder> {

    private Context context;
    private List<PreferenceData.Identifier.SectionIdentifier> sections;
    private List<PreferenceData> originalDatas, datas;

    public PreferenceSectionAdapter(Context context, List<PreferenceData> datas) {
        this.context = context;
        this.datas = datas;
        originalDatas = datas;

        sections = new ArrayList<>();
        for (PreferenceData data : datas) {
            PreferenceData.Identifier.SectionIdentifier section = data.getIdentifier().getSection();
            if (!sections.contains(section)) sections.add(section);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_section, null));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        TextView title = (TextView) holder.v.findViewById(R.id.title);
        RecyclerView recycler = (RecyclerView) holder.v.findViewById(R.id.recycler);

        title.setText(sections.get(position).name().replace('_', ' '));

        ArrayList<PreferenceData> items = getItems(sections.get(position));

        recycler.setNestedScrollingEnabled(false);
        recycler.setLayoutManager(new GridLayoutManager(context, 1));
        recycler.setAdapter(new PreferenceAdapter(context, items));

        if (items.size() > 0) holder.v.setVisibility(View.VISIBLE);
        else holder.v.setVisibility(View.GONE);

        ViewCompat.setElevation(holder.v.findViewById(R.id.background), StaticUtils.getPixelsFromDp(context, 2));
    }

    @Override
    public int getItemCount() {
        return sections.size();
    }

    private ArrayList<PreferenceData> getItems(PreferenceData.Identifier.SectionIdentifier section) {
        ArrayList<PreferenceData> datas = new ArrayList<>();
        for (PreferenceData data : this.datas) {
            if (data.getIdentifier().getSection() == section) datas.add(data);
        }
        return datas;
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

    public void filter(@Nullable String string) {
        if (string != null && string.length() > 0) {
            string = string.toLowerCase();

            ArrayList<PreferenceData> newDatas = new ArrayList<>();
            for (PreferenceData data : originalDatas) {
                PreferenceData.Identifier identifier = data.getIdentifier();

                String title = identifier.getTitle();
                if (title != null && title.toLowerCase().contains(string.toLowerCase())) {
                    newDatas.add(data);
                    continue;
                }

                String subtitle = identifier.getSubtitle();
                if (subtitle != null && subtitle.toLowerCase().contains(string.toLowerCase())) {
                    newDatas.add(data);
                    continue;
                }

                if (string.contains(identifier.getPreference().toString().toLowerCase())) {
                    newDatas.add(data);
                    continue;
                }

                if (string.contains(identifier.getSection().toString().toLowerCase())) {
                    newDatas.add(data);
                }
            }

            datas = newDatas;
        } else datas = originalDatas;
        notifyDataSetChanged();
    }
}
