package com.james.status.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.support.v7.app.AlertDialog;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.adapters.AppAdapter;
import com.james.status.data.AppData;
import com.james.status.data.AppPreferenceData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class AppPreferenceFragment extends SimpleFragment {

    private RecyclerView recycler;

    private AppAdapter adapter;
    private List<AppData> apps;

    public AppPreferenceFragment() {
        apps = new ArrayList<>();
    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apps, container, false);

        recycler = v.findViewById(R.id.recycler);

        Map<String, AppPreferenceData> apps = new HashMap<>();
        Map<String, ?> prefs = PreferenceManager.getDefaultSharedPreferences(getContext()).getAll();
        for (String key : prefs.keySet()) {
            for (String pref : new String[]{"/APP_COLOR", "/APP_FULLSCREEN", "/APP_FULLSCREEN_IGNORE"}) {
                if (key.endsWith(pref)) {
                    String component = key.substring(0, key.length() - pref.length());
                    if (!component.contains("/"))
                        apps.put(component, new AppPreferenceData(getContext(), component));
                }
            }
        }

        adapter = new AppAdapter(getContext(), new ArrayList<>(apps.values()));
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.setAdapter(adapter);

        return v;
    }

    public void reset() {
        new AlertDialog.Builder(getContext()).setTitle(R.string.reset_all).setMessage(R.string.reset_apps_confirm).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                for (AppData app : apps) {
                    app.clearPreferences(getContext());
                }

                StaticUtils.updateStatusService(getContext(), true);
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

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_apps);
    }
}
