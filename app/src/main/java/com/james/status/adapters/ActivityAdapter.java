package com.james.status.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.os.Looper;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.james.status.R;
import com.james.status.data.AppData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.ColorUtils;
import com.james.status.views.CustomImageView;

import java.util.List;

public class ActivityAdapter extends RecyclerView.Adapter<ActivityAdapter.ViewHolder> {

    private Context context;
    private List<AppData.ActivityData> activities;

    public ActivityAdapter(Context context, List<AppData.ActivityData> activites) {
        this.context = context;
        this.activities = activites;
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, null));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppData.ActivityData activity = getActivity(position);
        if (activity == null) return;

        ((TextView) holder.v.findViewById(R.id.appName)).setText(activity.label);
        ((TextView) holder.v.findViewById(R.id.appPackage)).setText(activity.name);

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Thread() {
            @Override
            public void run() {
                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                if (activity == null) return;

                final Drawable icon;
                try {
                    icon = context.getPackageManager().getApplicationIcon(activity.packageName);
                } catch (PackageManager.NameNotFoundException e) {
                    return;
                }

                new Handler(context.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (icon != null)
                            ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(icon);
                    }
                });
            }
        }.start();

        holder.v.findViewById(R.id.color).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                new Thread() {
                    @Override
                    public void run() {
                        AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                        if (activity == null) return;

                        final Integer color = ColorUtils.getPrimaryColor(context, activity.getComponentName());

                        new Handler(Looper.getMainLooper()).post(new Runnable() {
                            @Override
                            public void run() {
                                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                                if (activity == null) return;

                                new ColorPickerDialog(context).setTag(activity).setPreference(activity.getIntegerPreference(context, AppData.PreferenceIdentifier.COLOR)).setDefaultPreference(color != null ? color : Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                                    @Override
                                    public void onPreference(PreferenceDialog dialog, Integer preference) {
                                        Object tag = dialog.getTag();
                                        if (tag != null && tag instanceof AppData.ActivityData)
                                            ((AppData.ActivityData) tag).putPreference(context, AppData.PreferenceIdentifier.COLOR, preference);

                                        ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(preference));
                                    }

                                    @Override
                                    public void onCancel(PreferenceDialog dialog) {
                                    }
                                }).show();
                            }
                        });
                    }
                }.start();
            }
        });

        Integer color = activity.getIntegerPreference(context, AppData.PreferenceIdentifier.COLOR);
        if (color != null) {
            ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(color));

            holder.v.findViewById(R.id.titleBar).setBackgroundColor(color);
            ((TextView) holder.v.findViewById(R.id.appName)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
            ((TextView) holder.v.findViewById(R.id.appPackage)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(color) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
        } else {
            new Thread() {
                @Override
                public void run() {
                    AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                    if (activity == null) return;

                    final Integer color = ColorUtils.getPrimaryColor(context, activity.getComponentName());

                    new Handler(Looper.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            int someColor = color != null ? color : Color.BLACK;

                            ((ImageView) holder.v.findViewById(R.id.colorView)).setImageDrawable(new ColorDrawable(someColor));

                            holder.v.findViewById(R.id.titleBar).setBackgroundColor(someColor);
                            ((TextView) holder.v.findViewById(R.id.appName)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(someColor) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
                            ((TextView) holder.v.findViewById(R.id.appPackage)).setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(someColor) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                        }
                    });
                }
            }.start();
        }

        SwitchCompat fullscreenSwitch = (SwitchCompat) holder.v.findViewById(R.id.fullscreenSwitch);
        fullscreenSwitch.setOnCheckedChangeListener(null);

        Boolean isFullscreen = activity.getBooleanPreference(context, AppData.PreferenceIdentifier.FULLSCREEN);
        fullscreenSwitch.setChecked(isFullscreen != null && isFullscreen);

        fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                if (activity == null) return;

                activity.putPreference(context, AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
            }
        });

        holder.v.findViewById(R.id.notificationSwitch).setVisibility(View.GONE);
    }

    @Nullable
    private AppData.ActivityData getActivity(int position) {
        if (position < 0 || position >= activities.size()) return null;
        else return activities.get(position);
    }

    @Override
    public int getItemCount() {
        return activities.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
        }
    }
}
