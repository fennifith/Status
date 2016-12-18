package com.james.status.adapters;

import android.content.Context;
import android.graphics.Color;
import android.support.v7.widget.AppCompatRadioButton;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.data.icon.IconData;
import com.james.status.dialogs.IconStyleDialog;
import com.james.status.views.CustomImageView;

import java.util.List;

public class IconStyleAdapter extends RecyclerView.Adapter<IconStyleAdapter.ViewHolder> {

    private Context context;
    private IconData icon;
    private IconStyleData style;
    private List<IconStyleData> styles;
    private OnCheckedChangeListener onCheckedChangeListener;

    public IconStyleAdapter(Context context, IconData icon, List<IconStyleData> styles, OnCheckedChangeListener onCheckedChangeListener) {
        this.context = context;
        this.icon = icon;
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_icon_style, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppCompatRadioButton button = (AppCompatRadioButton) holder.v.findViewById(R.id.radio);
        IconStyleData style = styles.get(position);

        button.setText(style.name);
        button.setChecked(style.equals(this.style));

        LinearLayout layout = (LinearLayout) holder.v.findViewById(R.id.icons);
        layout.removeAllViewsInLayout();

        for (int i = 0; i < style.getSize(); i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_icon, layout, false);
            ((CustomImageView) view.findViewById(R.id.icon)).setImageDrawable(style.getDrawable(context, i), Color.BLACK);

            layout.addView(view);
        }

        if (layout.getChildCount() < 1) layout.setVisibility(View.GONE);

        if (style.type == IconStyleData.TYPE_FILE) {
            View edit = holder.v.findViewById(R.id.edit);
            edit.setVisibility(View.VISIBLE);

            edit.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    int position = holder.getAdapterPosition();
                    if (position < 0 || position >= styles.size()) return;

                    IconStyleData style = styles.get(position);
                    icon.removeIconStyle(style);
                    styles.remove(position);
                    if (styles.size() > 0) setIconStyle(styles.get(0));
                    else notifyDataSetChanged();

                    new IconStyleDialog(context, style, icon.getStringArrayPreference(IconData.PreferenceIdentifier.ICON_STYLE_NAMES)).setListener(new IconStyleDialog.OnIconStyleListener() {
                        @Override
                        public void onIconStyle(IconStyleData style) {
                            icon.addIconStyle(style);
                            styles = icon.getIconStyles();
                            setIconStyle(style);
                        }
                    }).show();
                }
            });
        } else holder.v.findViewById(R.id.edit).setVisibility(View.GONE);

        holder.v.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                int position = holder.getAdapterPosition();
                if (position >= 0 && position < styles.size()) setIconStyle(styles.get(position));
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
