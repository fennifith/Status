package com.james.status.adapters;

import android.content.Context;
import android.os.Handler;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentStatePagerAdapter;
import android.support.v4.view.ViewPager;

import com.james.status.fragments.SimpleFragment;

public class SimplePagerAdapter extends FragmentStatePagerAdapter {

    private Context context;
    private SimpleFragment[] fragments;

    public SimplePagerAdapter(Context context, FragmentManager fm, ViewPager viewPager, final SimpleFragment... fragments) {
        super(fm);
        this.context = context;
        this.fragments = fragments;

        viewPager.addOnPageChangeListener(new ViewPager.OnPageChangeListener() {
            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                fragments[position].onExitScroll(positionOffset);
                if (position + 1 < fragments.length)
                    fragments[position + 1].onEnterScroll(1 - positionOffset);
            }

            @Override
            public void onPageSelected(int position) {
                fragments[position].onSelect();
            }

            @Override
            public void onPageScrollStateChanged(int state) {
            }
        });

        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                if (fragments.length > 0) fragments[0].onSelect();
            }
        }, 500);
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
