/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.adapters;

import android.content.Context;
import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.dialogs.IconCreatorDialog;
import com.james.status.views.CustomImageView;

import java.util.List;

import androidx.appcompat.widget.AppCompatRadioButton;
import androidx.recyclerview.widget.RecyclerView;

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
        IconStyleData style = styles.get(position);

        holder.button.setText(style.name);
        holder.button.setChecked(style.equals(this.style));

        holder.layout.removeAllViewsInLayout();

        for (int i = 0; i < style.getSize(); i++) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_icon, holder.layout, false);
            ((CustomImageView) view.findViewById(R.id.icon)).setImageDrawable(style.getDrawable(context, i), Color.BLACK);

            holder.layout.addView(view);
        }

        if (holder.layout.getChildCount() < 1)
            holder.layout.setVisibility(View.GONE);

        if (style.type == IconStyleData.TYPE_FILE) {
            holder.edit.setVisibility(View.VISIBLE);
            holder.edit.setOnClickListener(view -> {
                int position1 = holder.getAdapterPosition();
                if (position1 < 0 || position1 >= styles.size()) return;

                IconStyleData style1 = styles.get(position1);
                icon.removeIconStyle(style1);
                styles.remove(position1);
                if (styles.size() > 0) setIconStyle(styles.get(0));
                else notifyDataSetChanged();

                new IconCreatorDialog(context, style1, (String[]) PreferenceData.ICON_ICON_STYLE_NAMES.getSpecificValue(context, icon.getIdentifierArgs()), icon.getIconNames()).setListener(new IconCreatorDialog.OnIconStyleListener() {
                    @Override
                    public void onIconStyle(IconStyleData style1) {
                        icon.addIconStyle(style1);
                        styles = icon.getIconStyles();
                        setIconStyle(style1);
                    }
                }).show();
            });
        } else holder.edit.setVisibility(View.GONE);

        holder.v.setOnClickListener(view -> {
            int position12 = holder.getAdapterPosition();
            if (position12 >= 0 && position12 < styles.size()) setIconStyle(styles.get(position12));
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

        View v, edit;
        AppCompatRadioButton button;
        LinearLayout layout;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            edit = v.findViewById(R.id.edit);
            button = v.findViewById(R.id.radio);
            layout = v.findViewById(R.id.icons);
        }
    }

}
