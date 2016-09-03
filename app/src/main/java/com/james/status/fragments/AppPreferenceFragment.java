package com.james.status.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.adapters.AppAdapter;

public class AppPreferenceFragment extends SimpleFragment {

    private AppAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_apps, container, false);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new AppAdapter(getContext());
        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void filter(@Nullable String filter) {
        adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_apps);
    }
}
