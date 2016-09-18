package com.james.status.fragments;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ProgressBar;

import com.james.status.R;
import com.james.status.adapters.AppAdapter;
import com.james.status.data.AppData;

import java.util.ArrayList;
import java.util.List;

public class AppPreferenceFragment extends SimpleFragment {

    private RecyclerView recycler;
    private ProgressBar progressBar;

    private AppAdapter adapter;
    private List<AppData> apps;
    private PackageManager packageManager;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apps, container, false);

        recycler = (RecyclerView) v.findViewById(R.id.recycler);
        progressBar = (ProgressBar) v.findViewById(R.id.progressBar);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        progressBar.setVisibility(View.VISIBLE);

        apps = new ArrayList<>();
        packageManager = getContext().getPackageManager();

        new Thread() {
            @Override
            public void run() {
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

                Context context = getContext();
                if (context != null) {
                    new Handler(context.getMainLooper()).post(new Runnable() {
                        @Override
                        public void run() {
                            adapter = new AppAdapter(getContext(), apps);
                            recycler.setAdapter(adapter);
                            progressBar.setVisibility(View.GONE);
                        }
                    });
                }
            }
        }.start();

        return v;
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
