package com.james.status.fragments;

import android.content.Context;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public abstract class SimpleFragment extends Fragment {

    public abstract String getTitle(Context context);

    public abstract void filter(@Nullable String filter);

    public void onSelect() {
    }

    public void onEnterScroll(float offset) {
    }

    public void onExitScroll(float offset) {
    }
}
