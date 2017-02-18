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
import com.james.status.Status;
import com.james.status.adapters.IconAdapter;

public class IconPreferenceFragment extends SimpleFragment implements Status.OnPreferenceChangedListener {

    private Status status;
    private IconAdapter adapter;
    private boolean isSelected;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_icons, container, false);
        status = (Status) getContext().getApplicationContext();
        status.addListener(this);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);
        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));

        adapter = new IconAdapter(getActivity());
        recycler.setAdapter(adapter);

        return v;
    }

    @Override
    public void onDestroy() {
        if (status != null) status.removeListener(this);
        super.onDestroy();
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_icons);
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
        if (adapter != null) {
            adapter.filter(filter);
        }
    }

    @Override
    public void onPreferenceChanged() {
        if (adapter != null) adapter.notifyDataSetChanged();
    }
}
