package com.james.status.adapters;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.SwitchCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CompoundButton;
import android.widget.ImageView;
import android.widget.TextView;

import com.afollestad.async.Action;
import com.afollestad.async.Async;
import com.afollestad.async.Done;
import com.afollestad.async.Result;
import com.james.status.R;
import com.james.status.activities.AppSettingActivity;
import com.james.status.data.AppData;
import com.james.status.data.PreferenceData;
import com.james.status.dialogs.ColorPickerDialog;
import com.james.status.dialogs.FullscreenEditorDialog;
import com.james.status.dialogs.PreferenceDialog;
import com.james.status.utils.ColorUtils;
import com.james.status.utils.StaticUtils;
import com.james.status.utils.StringUtils;
import com.james.status.views.ColorView;
import com.james.status.views.CustomImageView;

import java.util.Collections;
import java.util.Comparator;
import java.util.List;

public class AppAdapter extends RecyclerView.Adapter<AppAdapter.ViewHolder> {

    private Context context;
    private PackageManager packageManager;
    private List<AppData> apps;

    public AppAdapter(Context context, List<AppData> apps) {
        this.context = context;
        packageManager = context.getPackageManager();

        this.apps = apps;
        Collections.sort(apps, new Comparator<AppData>() {
            @Override
            public int compare(AppData lhs, AppData rhs) {
                return lhs.label.compareToIgnoreCase(rhs.label);
            }
        });
    }

    @Override
    public ViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new ViewHolder(LayoutInflater.from(context).inflate(R.layout.item_app_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final ViewHolder holder, int position) {
        AppData app = getApp(position);
        if (app == null) return;

        holder.name.setText(app.label);
        holder.packageName.setText(app.packageName);

        holder.icon.setImageDrawable(new ColorDrawable(Color.TRANSPARENT));

        new Action<Drawable>() {
            @NonNull
            @Override
            public String id() {
                return "appIcon";
            }

            @Nullable
            @Override
            protected Drawable run() throws InterruptedException {
                AppData app = getApp(holder.getAdapterPosition());
                if (app != null) {
                    try {
                        return packageManager.getApplicationIcon(app.packageName);
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
                                AppData app = getApp(holder.getAdapterPosition());
                                if (app != null)
                                    return app.getColor(context);
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
                                AppData app = getApp(holder.getAdapterPosition());
                                if (app != null)
                                    return app.getDefaultColor(context);
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
                                AppData app = getApp(holder.getAdapterPosition());
                                if (app != null)
                                    return ColorUtils.getColors(context, app);
                                else return null;
                            }
                        }
                ).done(new Done() {
                    @Override
                    public void result(@NonNull Result result) {
                        Action colorAction = result.get("color");
                        Action defaultColorAction = result.get("defaultColor");
                        Action colorsAction = result.get("colors");

                        AppData app = getApp(holder.getAdapterPosition());
                        if (app == null) return;

                        ColorPickerDialog dialog = new ColorPickerDialog(context).withAlpha((Boolean) PreferenceData.STATUS_TRANSPARENT_MODE.getValue(context));
                        if (colorsAction != null && colorsAction.getResult() != null)
                            dialog.setPresetColors((List<Integer>) colorsAction.getResult());
                        if (colorAction != null && colorAction.getResult() != null)
                            dialog.setPreference((Integer) colorAction.getResult());
                        if (defaultColorAction != null && defaultColorAction.getResult() != null)
                            dialog.setDefaultPreference((Integer) defaultColorAction.getResult());

                        dialog.setTag(app);
                        dialog.setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                            @Override
                            public void onPreference(PreferenceDialog dialog, Integer preference) {
                                Object tag = dialog.getTag();
                                if (tag != null && tag instanceof AppData)
                                    ((AppData) tag).putPreference(context, AppData.PreferenceIdentifier.COLOR, preference);

                                notifyItemChanged(holder.getAdapterPosition());
                            }

                            @Override
                            public void onCancel(PreferenceDialog dialog) {
                            }
                        });

                        dialog.setTitle(app.label);
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
                AppData app = getApp(holder.getAdapterPosition());
                if (app != null)
                    return app.getColor(context);
                else return null;
            }

            @Override
            protected void done(@Nullable Integer result) {
                if (result != null) {
                    holder.colorView.setColor(result);

                    holder.titleBar.setBackgroundColor(result);
                    holder.name.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(result) ? R.color.textColorPrimaryInverse : R.color.textColorPrimary));
                    holder.packageName.setTextColor(ContextCompat.getColor(context, ColorUtils.isColorDark(result) ? R.color.textColorSecondaryInverse : R.color.textColorSecondary));
                }
            }
        }.execute();

        Boolean isFullscreen = app.getBooleanPreference(context, AppData.PreferenceIdentifier.FULLSCREEN);
        holder.fullscreenImage.setImageResource(isFullscreen != null && isFullscreen ? R.drawable.ic_fullscreen : R.drawable.ic_fullscreen_exit);
        holder.fullscreen.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app != null) {
                    FullscreenEditorDialog dialog = new FullscreenEditorDialog(context, app);
                    dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                        @Override
                        public void onDismiss(DialogInterface dialog) {
                            notifyItemChanged(holder.getAdapterPosition());
                        }
                    });
                    dialog.show();
                }
            }
        });

        holder.notificationSwitch.setOnCheckedChangeListener(null);

        Boolean isNotifications = app.getSpecificBooleanPreference(context, AppData.PreferenceIdentifier.NOTIFICATIONS);
        holder.notificationSwitch.setChecked(isNotifications == null || isNotifications);

        holder.notificationSwitch.setOnCheckedChangeListener(new CompoundButton.OnCheckedChangeListener() {
            @Override
            public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
                AppData app = getApp(holder.getAdapterPosition());
                if (app == null) return;

                app.putSpecificPreference(context, AppData.PreferenceIdentifier.NOTIFICATIONS, isChecked);

                StaticUtils.updateStatusService(context, true);
            }
        });

        holder.launch.setVisibility(app.activities.size() > 1 ? View.VISIBLE : View.GONE);
        holder.launchText.setText(String.format(context.getString(R.string.msg_show_individual_screens), app.activities.size()));
        holder.launch.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(context, AppSettingActivity.class);
                intent.putExtra(AppSettingActivity.EXTRA_APP, getApp(holder.getAdapterPosition()));

                ActivityCompat.startActivity(context, intent, ActivityOptionsCompat.makeScaleUpAnimation(view, (int) view.getX(), (int) view.getY(), view.getWidth(), view.getHeight()).toBundle());
            }
        });

        holder.v.setAlpha(0);
        holder.v.animate().alpha(1).setDuration(500).start();
    }

    @Nullable
    private AppData getApp(int position) {
        if (position < 0 || position >= apps.size()) return null;
        else return apps.get(position);
    }

    @Override
    public int getItemCount() {
        return apps.size();
    }

    public void filter(@Nullable final String string) {
        if (string == null || string.length() < 1) {
            Collections.sort(apps, new Comparator<AppData>() {
                @Override
                public int compare(AppData lhs, AppData rhs) {
                    return lhs.label.compareToIgnoreCase(rhs.label);
                }
            });
        } else {
            Collections.sort(apps, new Comparator<AppData>() {
                @Override
                public int compare(AppData lhs, AppData rhs) {
                    int value = 0;

                    value += StringUtils.difference(lhs.label.toLowerCase(), string).length();
                    value += StringUtils.difference(lhs.packageName, string).length();
                    value -= StringUtils.difference(rhs.label.toLowerCase(), string).length();
                    value -= StringUtils.difference(rhs.packageName, string).length();

                    return value;
                }
            });
        }

        notifyDataSetChanged();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {

        View v, titleBar;
        TextView name, packageName;
        CustomImageView icon, launchIcon;
        ColorView colorView;
        View launch;
        TextView launchText;
        View color;
        View notifications;
        SwitchCompat notificationSwitch;
        View fullscreen;
        ImageView fullscreenImage;

        public ViewHolder(View v) {
            super(v);
            this.v = v;
            titleBar = v.findViewById(R.id.titleBar);
            name = v.findViewById(R.id.appName);
            packageName = v.findViewById(R.id.appPackage);
            icon = v.findViewById(R.id.icon);
            launch = v.findViewById(R.id.launch);
            launchIcon = v.findViewById(R.id.launchIcon);
            launchText = v.findViewById(R.id.launchText);
            color = v.findViewById(R.id.color);
            colorView = v.findViewById(R.id.colorView);
            notifications = v.findViewById(R.id.notifications);
            notificationSwitch = v.findViewById(R.id.notificationSwitch);
            fullscreen = v.findViewById(R.id.fullscreen);
            fullscreenImage = v.findViewById(R.id.fullscreenImage);
        }
    }
}
