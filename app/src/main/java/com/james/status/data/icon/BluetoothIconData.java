package com.james.status.data.icon;

import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;
import com.james.status.utils.StaticUtils;

import java.util.Arrays;
import java.util.List;

public class BluetoothIconData extends IconData<BluetoothIconData.BluetoothReceiver> {

    public BluetoothIconData(Context context) {
        super(context);
    }

    @Override
    public BluetoothIconData.BluetoothReceiver getReceiver() {
        return new BluetoothReceiver(this);
    }

    @Override
    public IntentFilter getIntentFilter() {
        return new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);
    }

    @Override
    public void register() {
        super.register();

        int state = StaticUtils.getBluetoothState(getContext());

        if (state != BluetoothAdapter.STATE_OFF)
            onDrawableUpdate(state == BluetoothAdapter.STATE_CONNECTED ? 1 : 0);
        else onDrawableUpdate(-1);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_bluetooth);
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
                                R.drawable.ic_bluetooth,
                                R.drawable.ic_bluetooth_connected
                        )
                )
        );

        return styles;
    }

    static class BluetoothReceiver extends IconUpdateReceiver<BluetoothIconData> {

        private BluetoothReceiver(BluetoothIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(BluetoothIconData icon, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state != BluetoothAdapter.STATE_OFF)
                icon.onDrawableUpdate(state == BluetoothAdapter.STATE_CONNECTED ? 1 : 0);
            else icon.onDrawableUpdate(-1);
        }
    }
}
