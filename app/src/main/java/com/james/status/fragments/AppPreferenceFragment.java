package com.james.status.fragments;

import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences;
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
import com.james.status.data.AppPreferenceData;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class AppPreferenceFragment extends SimpleFragment {

    private RecyclerView recycler;

    private AppAdapter adapter;
    private View emptyView;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apps, container, false);

        emptyView = v.findViewById(R.id.empty);
        recycler = v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        updateItems();
        return v;
    }

    public void updateItems() {
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
        recycler.setAdapter(adapter);

        emptyView.setVisibility(apps.size() > 0 ? View.GONE : View.VISIBLE);
    }

    public void reset() {
        new AlertDialog.Builder(getContext()).setTitle(R.string.reset_all).setMessage(R.string.reset_apps_confirm).setPositiveButton(R.string.action_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                SharedPreferences preferences = PreferenceManager.getDefaultSharedPreferences(getContext());
                Map<String, ?> prefs = preferences.getAll();
                SharedPreferences.Editor editor = preferences.edit();
                for (String key : prefs.keySet()) {
                    for (String pref : new String[]{"/APP_COLOR", "/APP_FULLSCREEN", "/APP_FULLSCREEN_IGNORE"}) {
                        if (key.endsWith(pref))
                            editor.remove(key);
                    }
                }

                editor.apply();
                updateItems();
                dialogInterface.dismiss();
            }
        }).setNegativeButton(R.string.action_cancel, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialogInterface, int i) {
                dialogInterface.dismiss();
            }
        }).show();
    }

    public void showDialog() {

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
