package com.james.status.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.preference.PreferenceData;
import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class PreferenceSectionAdapter extends RecyclerView.Adapter<PreferenceSectionAdapter.ViewHolder> {

    private Context context;
    private List<PreferenceData.Identifier.SectionIdentifier> sections;
    private List<PreferenceData> originalDatas, datas;

    public PreferenceSectionAdapter(Context context, List<PreferenceData> datas) {
        this.context = context;

        originalDatas = new ArrayList<>();
        originalDatas.addAll(datas);

        this.datas = new ArrayList<>();
        this.datas.addAll(originalDatas);

        sections = new ArrayList<>();
        for (PreferenceData data : datas) {
            PreferenceData.Identifier.SectionIdentifier section = data.getIdentifier().getSection();
            if (!sections.contains(section)) sections.add(section);
        }
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_preference_section, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(sections.get(position).name().replace('_', ' '));

        ArrayList<PreferenceData> items = getItems(sections.get(position));

        holder.recycler.setNestedScrollingEnabled(false);
        holder.recycler.setLayoutManager(new GridLayoutManager(context, 1));
        holder.recycler.setAdapter(new PreferenceAdapter(context, items));

        if (items.size() > 0) holder.v.setVisibility(View.VISIBLE);
        else holder.v.setVisibility(View.GONE);

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
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
        TextView title;
        RecyclerView recycler;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            title = (TextView) v.findViewById(R.id.title);
            recycler = (RecyclerView) v.findViewById(R.id.recycler);
        }
    }

    public void filter(@Nullable String string) {
        if (string != null && string.length() > 0) {
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

                PreferenceUtils.PreferenceIdentifier preference = identifier.getPreference();
                if (preference != null && preference.toString().toLowerCase().contains(string)) {
                    newDatas.add(data);
                    continue;
                }

                PreferenceData.Identifier.SectionIdentifier section = identifier.getSection();
                if (section != null && string.contains(section.toString().toLowerCase())) {
                    newDatas.add(data);
                }
            }

            datas = newDatas;
        } else datas = originalDatas;
        notifyDataSetChanged();
    }
}
