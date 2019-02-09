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

package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.os.Build;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;

public class HeadphoneIconData extends IconData<HeadphoneIconData.HeadphoneReceiver> {

    public HeadphoneIconData(Context context) {
        super(context);
    }

    @Override
    public HeadphoneReceiver getReceiver() {
        return new HeadphoneReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            return new IntentFilter(AudioManager.ACTION_HEADSET_PLUG);
        else return new IntentFilter(Intent.ACTION_HEADSET_PLUG);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_headphone);
    }

    @Override
    public int getIconStyleSize() {
        return 2;
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = super.getIconStyles();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_headset,
                                R.drawable.ic_headset_mic
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_system),
                                IconStyleData.TYPE_IMAGE,
                                android.R.drawable.stat_sys_headset
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_earbuds),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_icons8_headset_earbuds
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_headphone_headphones),
                getContext().getString(R.string.icon_headphone_headset)
        };
    }

    static class HeadphoneReceiver extends IconUpdateReceiver<HeadphoneIconData> {

        private HeadphoneReceiver(HeadphoneIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(HeadphoneIconData icon, Intent intent) {
            if (intent.getIntExtra("state", 0) == 1)
                icon.onIconUpdate(intent.getIntExtra("microphone", 0));
            else icon.onIconUpdate(-1);
        }
    }
}
