package com.james.status.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.james.status.R;

public class IconPreferenceFragment extends SimpleFragment {

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View v = inflater.inflate(R.layout.fragment_icons, container, false);

        return v;
    }

    @Override
    public String getTitle(Context context) {
        return context.getString(R.string.tab_icons);
    }
}
