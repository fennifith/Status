package com.james.status.adapters;

import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.afollestad.async.Async;
import com.afollestad.async.Done;
import com.afollestad.async.Result;
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
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppData.ActivityData activity = getActivity(position);
        if (activity == null) return;

        holder.v.findViewById(R.id.launchIcon).setVisibility(View.GONE);

        holder.name.setText(activity.label);
        holder.packageName.setText(activity.name);

        ((CustomImageView) holder.v.findViewById(R.id.icon)).setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Action<Drawable>() {
            @NonNull
            @Override
            public String id() {
                return "appIcon";
            }

            @Nullable
            @Override
            protected Drawable run() throws InterruptedException {
                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                if (activity != null) {
                    try {
                        return context.getPackageManager().getApplicationIcon(activity.packageName);
                    } catch (PackageManager.NameNotFoundException ignored) {
                    }
                }

                return null;
            }

            @Override
            protected void done(@Nullable Drawable result) {
                if (result != null)
                    holder.icon.setImageDrawable(result);
            }
        }.execute();

        holder.color.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Async.parallel(
                        new Action<Integer>() {
                            @NonNull
                            @Override
                            public String id() {
                                return "color";
                            }

                            @Nullable
                            @Override
                            protected Integer run() throws InterruptedException {
                                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                                if (activity != null)
                                    return activity.getColor(context);
                                else return null;
                            }
                        },
                        new Action<Integer>() {
                            @NonNull
                            @Override
                            public String id() {
                                return "defaultColor";
                            }

                            @Nullable
                            @Override
                            protected Integer run() throws InterruptedException {
                                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                                if (activity != null)
                                    return activity.getDefaultColor(context);
                                else return null;
                            }
                        },
                        new Action<List<Integer>>() {
                            @NonNull
                            @Override
                            public String id() {
                                return "colors";
                            }

                            @Nullable
                            @Override
                            protected List<Integer> run() throws InterruptedException {
                                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                                if (activity != null)
                                    return ColorUtils.getColors(context, activity.packageName);
                                else return null;
                            }
                        }
                ).done(new Done() {
                    @Override
                    public void result(@NonNull Result result) {
                        Action colorAction = result.get("color");
                        Action defaultColorAction = result.get("defaultColor");
                        Action colorsAction = result.get("colors");

                        AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                        if (activity == null) return;

                        ColorPickerDialog dialog = new ColorPickerDialog(context);
                        if (colorsAction != null && colorsAction.getResult() != null)
                            dialog.setPresetColors((List<Integer>) colorsAction.getResult());
                        if (colorAction != null && colorAction.getResult() != null)
                            dialog.setPreference((Integer) colorAction.getResult());
                        if (defaultColorAction != null && defaultColorAction.getResult() != null)
                            dialog.setDefaultPreference((Integer) defaultColorAction.getResult());

                        dialog.setTag(activity);
                        dialog.setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                            @Override
                            public void onPreference(PreferenceDialog dialog, Integer preference) {
                                Object tag = dialog.getTag();
                                if (tag != null && tag instanceof AppData.ActivityData)
                                    ((AppData.ActivityData) tag).putPreference(context, AppData.PreferenceIdentifier.COLOR, preference);

                                notifyItemChanged(holder.getAdapterPosition());
                            }

                            @Override
                            public void onCancel(PreferenceDialog dialog) {
                            }
                        });

                        dialog.setTitle(activity.label);
                        dialog.show();
                    }
                });
            }
        });

        new Action<Integer>() {
            @NonNull
            @Override
            public String id() {
                return "color";
            }

            @Nullable
            @Override
            protected Integer run() throws InterruptedException {
                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                if (activity != null)
                    return activity.getColor(context);
                else return null;
            }

            @Override
            protected void done(@Nullable Integer result) {
                if (result != null) {
                    holder.colorView.setImageDrawable(new ColorDrawable(result));

                    holder.titleBar.setBackgroundColor(result);
                    holder.name.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(result) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
                    holder.packageName.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(result) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                }
            }
        }.execute();

        holder.fullscreenSwitch.setOnCheckedChangeListener(null);

        Boolean isFullscreen = activity.getBooleanPreference(context, AppData.PreferenceIdentifier.FULLSCREEN);
        holder.fullscreenSwitch.setChecked(isFullscreen != null && isFullscreen);
        holder.color.setVisibility(holder.fullscreenSwitch.isChecked() ? View.GONE : View.VISIBLE);

        holder.fullscreenSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppData.ActivityData activity = getActivity(holder.getAdapterPosition());
                if (activity == null) return;

                activity.putPreference(context, AppData.PreferenceIdentifier.FULLSCREEN, isChecked);
                holder.color.setVisibility(isChecked ? View.GONE : View.VISIBLE);
            }
        });

        holder.notificationSwitch.setVisibility(View.GONE);

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).setStartDelay(100 + (10 * position)).start();
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

        View v, titleBar;
        TextView name, packageName;
        CustomImageView icon, launchIcon, colorView;
        View color;
        View notifications;
        SwitchCompat fullscreenSwitch, notificationSwitch;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            titleBar = v.findViewById(R.id.titleBar);
            name = (TextView) v.findViewById(R.id.appName);
            packageName = (TextView) v.findViewById(R.id.appPackage);
            icon = (CustomImageView) v.findViewById(R.id.icon);
            launchIcon = (CustomImageView) v.findViewById(R.id.launchIcon);
            color = v.findViewById(R.id.color);
            colorView = (CustomImageView) v.findViewById(R.id.colorView);
            notifications = v.findViewById(R.id.notifications);
            notificationSwitch = (SwitchCompat) v.findViewById(R.id.notificationSwitch);
            fullscreenSwitch = (SwitchCompat) v.findViewById(R.id.fullscreenSwitch);
        }
    }
}
