package com.james.status.data.icon;

import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;

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
        return new RingerReceiver(this);
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
                onIconUpdate(0);
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                onIconUpdate(1);
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                onIconUpdate(-1);
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
                                R.drawable.ic_sound_vibrate
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_system),
                                IconStyleData.TYPE_IMAGE,
                                android.R.drawable.ic_lock_silent_mode
                        ),
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_android),
                                IconStyleData.TYPE_VECTOR,
                                R.drawable.ic_sound_android_mute,
                                R.drawable.ic_sound_android_vibrate
                        )
                )
        );

        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_ringer_muted),
                getContext().getString(R.string.icon_ringer_vibrate)
        };
    }

    static class RingerReceiver extends IconUpdateReceiver<RingerIconData> {

        private RingerReceiver(RingerIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(RingerIconData icon, Intent intent) {
            switch (icon.audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    icon.onIconUpdate(0);
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    icon.onIconUpdate(1);
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    icon.onIconUpdate(-1);
                    break;
            }
        }
    }
}
