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

import com.getkeepsafe.taptargetview.TapTargetView;
import com.james.status.R;
import com.james.status.adapters.AppAdapter;
import com.james.status.data.AppData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.List;

public class AppPreferenceFragment extends SimpleFragment {

    private RecyclerView recycler;
    private ProgressBar progressBar;

    private AppAdapter adapter;
    private List<AppData> apps;
    private PackageManager packageManager;

    private boolean isSelected;

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

        recycler.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrollStateChanged(RecyclerView recyclerView, int newState) {
                super.onScrollStateChanged(recyclerView, newState);
                if (adapter != null && adapter.iconView != null && newState == RecyclerView.SCROLL_STATE_IDLE && isSelected) {
                    if (StaticUtils.shouldShowTutorial(getContext(), "activities")) {
                        new TapTargetView.Builder(getActivity())
                                .title(R.string.tutorial_activities)
                                .description(R.string.tutorial_activities_desc)
                                .outerCircleColor(R.color.colorPrimary)
                                .dimColor(android.R.color.black)
                                .drawShadow(false)
                                .listener(new TapTargetView.Listener() {
                                    @Override
                                    public void onTargetClick(TapTargetView view) {
                                        view.dismiss(true);
                                        adapter.iconView.performClick();
                                    }

                                    @Override
                                    public void onTargetLongClick(TapTargetView view) {
                                    }
                                })
                                .cancelable(true)
                                .showFor(adapter.iconView);
                    }
                }
            }
        });

        return v;
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
