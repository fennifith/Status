package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.text.method.LinkMovementMethod;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import com.james.status.R;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LicenseAdapter extends RecyclerView.Adapter<LicenseAdapter.ViewHolder> {

    private Context context;
    private List<CharSequence> licenses;

    public LicenseAdapter(Context context) {
        this.context = context;

        licenses = new ArrayList<>();
        licenses.addAll(Arrays.asList(context.getResources().getTextArray(R.array.libraries)));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_text, parent, false));
    }

    @Override
    public void onBindViewHolder(ViewHolder holder, int position) {
        holder.title.setVisibility(View.GONE);

        holder.subtitle.setText(licenses.get(position));
        holder.subtitle.setMovementMethod(new LinkMovementMethod());

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return licenses.size();
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
