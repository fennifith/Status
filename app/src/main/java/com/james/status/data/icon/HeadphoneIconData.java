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
                                android.R.drawable.stat_sys_headset,
                                android.R.drawable.stat_sys_headset
                        )
                )
        );

        return styles;
    }

    static class HeadphoneReceiver extends IconUpdateReceiver<HeadphoneIconData> {

        private HeadphoneReceiver(HeadphoneIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(HeadphoneIconData icon, Intent intent) {
            if (intent.getIntExtra("state", 0) == 1)
                icon.onDrawableUpdate(intent.getIntExtra("microphone", 0));
            else icon.onDrawableUpdate(-1);
        }
    }
}
