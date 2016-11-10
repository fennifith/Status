package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;

import java.util.Arrays;
import java.util.List;

public class RingerIconData extends IconData<RingerIconData.RingerReceiver> {

    private AudioManager audioManager;

    public RingerIconData(Context context) {
        super(context);
        audioManager = (AudioManager) context.getSystemService(Context.AUDIO_SERVICE);
    }

    @Override
    public RingerReceiver getReceiver() {
        return new RingerReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(AudioManager.RINGER_MODE_CHANGED_ACTION);
    }

    @Override
    public void register() {
        super.register();

        switch (audioManager.getRingerMode()) {
            case AudioManager.RINGER_MODE_SILENT:
                onDrawableUpdate(0);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                onDrawableUpdate(1);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                onDrawableUpdate(-1);
                break;
        }
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_ringer);
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
                                R.drawable.ic_sound_mute,
                                R.drawable.ic_sound_vibration
                        )
                )
        );

        return styles;
    }

    public class RingerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    onDrawableUpdate(0);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    onDrawableUpdate(1);
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    onDrawableUpdate(-1);
                    break;
            }
        }
    }
}
