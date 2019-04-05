/*
 *    Copyright 2019 James Fenn
 *
 *    Licensed under the Apache License, Version 2.0 (the "License");
 *    you may not use this file except in compliance with the License.
 *    You may obtain a copy of the License at
 *
 *        http://www.apache.org/licenses/LICENSE-2.0
 *
 *    Unless required by applicable law or agreed to in writing, software
 *    distributed under the License is distributed on an "AS IS" BASIS,
 *    WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 *    See the License for the specific language governing permissions and
 *    limitations under the License.
 */

package com.james.status.adapters;

import android.content.Context;
import android.os.Handler;

import com.james.status.fragments.SimpleFragment;

import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentStatePagerAdapter;
import androidx.viewpager.widget.ViewPager;

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

        new Handler().postDelayed(() -> {
            if (fragments.length > 0) fragments[0].onSelect();
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
