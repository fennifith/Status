package com.james.status.adapters;

import android.content.Context;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;

import com.james.status.fragments.SimpleFragment;

public class SimplePagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private SimpleFragment[] fragments;

    public SimplePagerAdapter(Context context, FragmentManager fm, SimpleFragment... fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;
    }

    @Override
    public Fragment getItem(int position) {
        return fragments[position];
    }

    @Override
    public int getCount() {
        return fragments.length;
    }

    @Override
    public CharSequence getPageTitle(int position) {
        return fragments[position].getTitle(context);
    }

    public void filter(int position, String filter) {
        fragments[position].filter(filter);
    }
}
