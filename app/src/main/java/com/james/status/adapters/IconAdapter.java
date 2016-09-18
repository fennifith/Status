package com.james.status.adapters;

import android.app.Activity;
import android.support.annotation.Nullable;
import android.support.v7.widget.AppCompatCheckBox;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;

import com.james.status.R;
import com.james.status.data.icon.IconData;
import com.james.status.services.StatusService;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class IconAdapter extends RecyclerView.Adapter<IconAdapter.ViewHolder> {

    private Activity activity;
    private List<IconData> originalIcons, icons;
    public AppCompatCheckBox checkBoxView;

    public IconAdapter(Activity activity) {
        this.activity = activity;
        setIcons(StatusService.getIcons(activity));
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(activity).inflate(R.layout.item_icon_preference, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        IconData icon = getIcon(position);
        if (icon == null) return;

        icon.putPreference(IconData.PreferenceIdentifier.POSITION, position);

        AppCompatCheckBox checkBox = (AppCompatCheckBox) holder.v.findViewById(R.id.iconCheckBox);
        checkBox.setText(icon.getTitle());

        checkBoxView = checkBox;

        checkBox.setOnCheckedChangeListener(null);

        Boolean isVisible = icon.getBooleanPreference(IconData.PreferenceIdentifier.VISIBILITY);
        checkBox.setChecked(isVisible == null || isVisible);

        checkBox.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                IconData icon = getIcon(holder.getAdapterPosition());
                if (icon == null) return;

                icon.putPreference(IconData.PreferenceIdentifier.VISIBILITY, isChecked);
                StaticUtils.updateStatusService(activity);

                notifyItemChanged(holder.getAdapterPosition());
            }
        });

        RecyclerView recycler = (RecyclerView) holder.v.findViewById(R.id.recycler);
        recycler.setVisibility(isVisible == null || isVisible ? View.VISIBLE : View.GONE);

        recycler.setLayoutManager(new GridLayoutManager(activity, 1));
        recycler.setNestedScrollingEnabled(false);
        recycler.setAdapter(new PreferenceAdapter(activity, icon.getPreferences()));
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
        icons.clear();

        if (filter == null || filter.length() < 1) {
            icons.addAll(originalIcons);
        } else {
            for (IconData icon : originalIcons) {
                if (icon.getTitle().toLowerCase().contains(filter))
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

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
