package com.james.status.fragments;

import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.support.v4.app.Fragment;

public class SimpleFragment extends Fragment {

    public String getTitle(Context context) {
        return "";
    }

    public Drawable getIcon(Context context) {
        return new ColorDrawable(Color.TRANSPARENT);
    }
}
