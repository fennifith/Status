package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.data.preference.ListPreferenceData;

import java.util.List;

public class ListPreferenceAdapter extends RecyclerView.Adapter<ListPreferenceAdapter.ViewHolder> {

    private Context context;
    private ListPreferenceData.ListPreference style;
    private List<ListPreferenceData.ListPreference> styles;
    private OnCheckedChangeListener onCheckedChangeListener;

    public ListPreferenceAdapter(Context context, List<ListPreferenceData.ListPreference> styles, OnCheckedChangeListener onCheckedChangeListener) {
        this.context = context;
        this.styles = styles;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setListPreference(ListPreferenceData.ListPreference iconStyle) {
        style = iconStyle;
        onCheckedChangeListener.onCheckedChange(iconStyle);

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_radio_button, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppCompatRadioButton button = (AppCompatRadioButton) holder.v.findViewById(R.id.radio);
        ListPreferenceData.ListPreference style = styles.get(position);

        button.setText(style.name);
        button.setChecked(style.equals(this.style));

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setListPreference(styles.get(holder.getAdapterPosition()));
            }
        });

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Override
    public int getItemCount() {
        return styles.size();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(ListPreferenceData.ListPreference selected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

}
