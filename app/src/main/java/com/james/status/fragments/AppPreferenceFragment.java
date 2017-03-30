package com.james.status.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.afollestad.async.Action;
import com.james.status.R;
import com.james.status.adapters.AppAdapter;
import com.james.status.data.AppData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

public class AppPreferenceFragment extends SimpleFragment {

    private RecyclerView recycler;
    private ProgressBar progressBar;

    private AppAdapter adapter;
    private List<AppData> apps;
    private PackageManager packageManager;

    private boolean isSelected;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        apps = new ArrayList<>();
        packageManager = getContext().getPackageManager();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apps, container, false);

        recycler = (RecyclerView) v.findViewById(R.id.recycler);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        progressBar.setVisibility(View.VISIBLE);

        new Action<List<AppData>>() {
            @NonNull
            @Override
            public String id() {
                return "apps";
            }

            @Nullable
            @Override
            protected List<AppData> run() throws InterruptedException {
                for (ApplicationInfo applicationInfo : packageManager.getInstalledApplications(PackageManager.GET_META_DATA)) {
                    PackageInfo packageInfo;

                    try {
                        packageInfo = packageManager.getPackageInfo(applicationInfo.packageName, PackageManager.GET_ACTIVITIES);
                    } catch (PackageManager.NameNotFoundException e) {
                        continue;
                    }

                    if (packageInfo.activities != null && packageInfo.activities.length > 0)
                        apps.add(new AppData(packageManager, applicationInfo, packageInfo));
                }

                return apps;
            }

            @Override
            protected void done(@Nullable List<AppData> result) {
                if (result != null && getContext() != null) {
                    adapter = new AppAdapter(getContext(), result);
                    recycler.setAdapter(adapter);
                    progressBar.setVisibility(View.GONE);
                }
            }
        }.execute();

        return v;
    }

    public void reset() {
        new AlertDialog.Builder(getContext()).setTitle(R.string.reset_all).setMessage(R.string.reset_apps_confirm).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (AppData app : apps) {
                    app.clearPreferences(getContext());
                }

                StaticUtils.updateStatusService(getContext());
                adapter.notifyDataSetChanged();
                dialogInterface.dismiss();
            }
        }).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    public boolean isNotifications() {
        boolean notifications = true;
        try {
            for (AppData app : apps) {
                Boolean isNotifications = app.getSpecificBooleanPreference(getContext(), AppData.PreferenceIdentifier.NOTIFICATIONS);
                if (isNotifications != null && !isNotifications) {
                    notifications = false;
                    break;
                }
            }
        } catch (ConcurrentModificationException ignored) {
        }

        return notifications;
    }

    public void setNotifications(boolean isNotifications) {
        for (AppData app : apps) {
            app.putSpecificPreference(getContext(), AppData.PreferenceIdentifier.NOTIFICATIONS, isNotifications);
        }

        StaticUtils.updateStatusService(getContext());
        adapter.notifyDataSetChanged();
    }

    @Override
    public void onSelect() {
        isSelected = true;
    }

    @Override
    public void onEnterScroll(float offset) {
        isSelected = offset == 0;
    }

    @Override
    public void onExitScroll(float offset) {
        isSelected = offset == 0;
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_apps);
    }
}
