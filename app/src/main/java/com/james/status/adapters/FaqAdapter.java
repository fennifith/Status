package com.james.status.adapters;

import android.content.Context;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.FaqData;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class FaqAdapter extends RecyclerView.Adapter<FaqAdapter.ViewHolder> {

    private Context context;
    private List<FaqData> originalFaqs, faqs;

    public FaqAdapter(Context context, List<FaqData> faqs) {
        this.context = context;

        originalFaqs = new ArrayList<>();
        originalFaqs.addAll(faqs);

        Collections.sort(this.originalFaqs, new Comparator<FaqData>() {
            @Override
            public int compare(FaqData lhs, FaqData rhs) {
                return lhs.name.compareTo(rhs.name);
            }
        });

        this.faqs = new ArrayList<>();
        this.faqs.addAll(originalFaqs);
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setText(faqs.get(position).name);
        holder.subtitle.setText(faqs.get(position).content);

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return faqs.size();
    }

    public void filter(@Nullable String filter) {
        faqs.clear();

        if (filter == null || filter.length() < 1) {
            faqs.addAll(originalFaqs);
        } else {
            for (FaqData faq : originalFaqs) {
                if (faq.name.toLowerCase().contains(filter) || filter.contains(faq.name.toLowerCase()) || faq.content.toLowerCase().contains(filter) || filter.contains(faq.content.toLowerCase()))
                    faqs.add(faq);
            }
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        TextView title, subtitle;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            title = (TextView) v.findViewById(R.id.title);
            subtitle = (TextView) v.findViewById(R.id.subtitle);
        }
    }
}
