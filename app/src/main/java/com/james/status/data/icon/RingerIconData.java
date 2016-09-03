package com.james.status.data.icon;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.media.AudioManager;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;

public class RingerIconData extends IconData<RingerIconData.RingerReceiver> {

    AudioManager audioManager;

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
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(0), getContext().getTheme()));
                break;
            case AudioManager.RINGER_MODE_VIBRATE:
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(1), getContext().getTheme()));
                break;
            case AudioManager.RINGER_MODE_NORMAL:
                onDrawableUpdate(null);
                break;
        }
    }

    @Override
    public int[] getDefaultIconResource() {
        return new int[]{
                R.drawable.ic_sound_mute,
                R.drawable.ic_sound_vibration
        };
    }

    public class RingerReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            switch (audioManager.getRingerMode()) {
                case AudioManager.RINGER_MODE_SILENT:
                    onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(0), getContext().getTheme()));
                    break;
                case AudioManager.RINGER_MODE_VIBRATE:
                    onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(1), getContext().getTheme()));
                    break;
                case AudioManager.RINGER_MODE_NORMAL:
                    onDrawableUpdate(null);
                    break;
            }
        }
    }
}
