package com.james.status.adapters;

import android.content.Context;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.views.IconStyleImageView;

import java.util.List;

public class IconStyleAdapter extends RecyclerView.Adapter<IconStyleAdapter.ViewHolder> {

    private Context context;
    private IconStyleData style;
    private List<IconStyleData> styles;
    private OnCheckedChangeListener onCheckedChangeListener;

    public IconStyleAdapter(Context context, List<IconStyleData> styles, OnCheckedChangeListener onCheckedChangeListener) {
        this.context = context;
        this.styles = styles;
        this.onCheckedChangeListener = onCheckedChangeListener;
    }

    public void setIconStyle(IconStyleData iconStyle) {
        style = iconStyle;
        onCheckedChangeListener.onCheckedChange(iconStyle);

        notifyDataSetChanged();
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_icon_style, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppCompatRadioButton button = (AppCompatRadioButton) holder.v.findViewById(R.id.radio);
        IconStyleData style = styles.get(position);

        button.setText(style.name);
        button.setChecked(style.equals(this.style));

        ((IconStyleImageView) holder.v.findViewById(R.id.icon)).setIconStyle(style);

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                setIconStyle(styles.get(holder.getAdapterPosition()));
            }
        });
    }

    @Override
    public int getItemCount() {
        return styles.size();
    }

    public interface OnCheckedChangeListener {
        void onCheckedChange(IconStyleData selected);
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }

}
