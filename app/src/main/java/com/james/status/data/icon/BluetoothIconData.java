package com.james.status.data.icon;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.utils.PreferenceUtils;
import com.james.status.utils.StaticUtils;

public class BluetoothIconData extends IconData<BluetoothIconData.BluetoothReceiver> {

    public BluetoothIconData(Context context, PreferenceUtils.PreferenceIdentifier identifier, DrawableListener drawableListener) {
        super(context, identifier, drawableListener);
    }

    @Override
    public BluetoothIconData.BluetoothReceiver getReceiver() {
        return new BluetoothReceiver();
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void register() {
        super.register();

        int state = StaticUtils.getBluetoothState(getContext());

        if (state != BluetoothAdapter.STATE_OFF) {
            if (state == BluetoothAdapter.STATE_CONNECTED)
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(1), getContext().getTheme()));
            else
                onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(0), getContext().getTheme()));
        } else onDrawableUpdate(null);
    }

    @Override
    public IconStyleData getDefaultIconStyle() {
        return new IconStyleData(
                getContext().getString(R.string.icon_style_default),
                R.drawable.ic_bluetooth,
                R.drawable.ic_bluetooth_connected
        );
    }

    public class BluetoothReceiver extends BroadcastReceiver {
        @Override
        public void onReceive(Context context, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state != BluetoothAdapter.STATE_OFF) {
                if (state == BluetoothAdapter.STATE_CONNECTED)
                    onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(1), getContext().getTheme()));
                else
                    onDrawableUpdate(VectorDrawableCompat.create(getContext().getResources(), getIconResource(0), getContext().getTheme()));
            } else onDrawableUpdate(null);
        }
    }
}
