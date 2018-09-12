package com.james.status.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.support.v7.widget.helper.ItemTouchHelper;
import android.view.HapticFeedbackConstants;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.services.StatusServiceImpl;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private Activity activity;
    private List<IconData> originalIcons, icons;
    private String filter;
    public View itemView;

    private ItemTouchHelper helper;

    public IconAdapter(Activity activity, ItemTouchHelper helper) {
        this.activity = activity;
        this.helper = helper;
        setIcons(StatusServiceImpl.getIcons(activity));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_icon_preference, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        IconData icon = getIcon(position);
        if (icon == null) return;

        itemView = holder.v;

        PreferenceData.ICON_POSITION.setValue(activity, position, icon.getIdentifierArgs());

        holder.checkBox.setText(icon.getTitle());
        holder.checkBox.setOnCheckedChangeListener(null);

        boolean isVisible = icon.isVisible();
        holder.checkBox.setChecked(isVisible);

        holder.checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IconData icon = getIcon(holder.getAdapterPosition());
                if (icon == null) return;

                if (isChecked && !StaticUtils.isPermissionsGranted(activity, icon.getPermissions())) {
                    //TODO: show explanation dialog (with option to skip)
                    StaticUtils.requestPermissions(activity, icon.getPermissions());
                    holder.checkBox.setOnCheckedChangeListener(null);
                    holder.checkBox.setChecked(false);
                    holder.checkBox.setOnCheckedChangeListener(this);
                } else {
                    PreferenceData.ICON_VISIBILITY.setValue(activity, isChecked, icon.getIdentifierArgs());
                    StaticUtils.updateStatusService(activity, false);

                    notifyItemChanged(holder.getAdapterPosition());
                }
            }
        });

        holder.dragView.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN)
                    helper.startDrag(holder);

                return false;
            }
        });

        holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                helper.startDrag(holder);
                return false;
            }
        });
    }

    @Override
    public int getItemCount() {
        return icons.size();
    }

    @Nullable
    private IconData getIcon(int position) {
        if (position < 0 || position >= icons.size()) return null;
        else return icons.get(position);
    }

    public void filter(@Nullable String filter) {
        this.filter = filter;
        icons.clear();

        if (filter == null || filter.length() < 1) {
            icons.addAll(originalIcons);
        } else {
            for (IconData icon : originalIcons) {
                if (icon.getTitle().toLowerCase().contains(filter) || filter.contains(icon.getTitle().toLowerCase()))
                    icons.add(icon);
            }
        }

        notifyDataSetChanged();
    }

    public List<IconData> getIcons() {
        List<IconData> icons = new ArrayList<>();
        icons.addAll(originalIcons);
        return icons;
    }

    public void setIcons(List<IconData> icons) {
        originalIcons = new ArrayList<>();
        originalIcons.addAll(icons);

        this.icons = new ArrayList<>();
        this.icons.addAll(originalIcons);
    }

    public void notifyIconsChanged(IconData... icons) {
        for (IconData icon : icons) {
            if (this.icons.contains(icon))
                notifyItemChanged(this.icons.indexOf(icon));
        }
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;
        SwitchCompat checkBox;
        View dragView;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            checkBox = v.findViewById(R.id.iconSwitch);
            dragView = v.findViewById(R.id.drag);
        }
    }
}
