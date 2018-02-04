package com.james.status.fragments;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.GridLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;
import com.james.status.adapters.ArrayAdapter;

public class HelpFragment extends SimpleFragment {

    private static final String COMMUNITY_URL = "https://plus.google.com/communities/100021389226995148571";

    private ArrayAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_help, container, false);

        RecyclerView recycler = (RecyclerView) v.findViewById(R.id.recycler);

        recycler.setLayoutManager(new GridLayoutManager(getContext(), 1));
        recycler.setNestedScrollingEnabled(false);

        adapter = new ArrayAdapter(getContext(), R.array.faq);
        recycler.setAdapter(adapter);

        v.findViewById(R.id.community).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(COMMUNITY_URL)));
            }
        });

        return v;
    }

    @Override
    public void filter(@Nullable String filter) {
        if (adapter != null) adapter.filter(filter);
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_help);
    }
}
