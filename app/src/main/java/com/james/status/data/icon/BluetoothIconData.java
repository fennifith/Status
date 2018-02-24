package com.james.status.data.icon;

import android.Manifest;
import android.bluetooth.BluetoothAdapter;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.receivers.IconUpdateReceiver;
import com.james.status.utils.StaticUtils;

import java.util.Arrays;
import java.util.Collections;
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
            onIconUpdate(state == BluetoothAdapter.STATE_CONNECTED ? 1 : 0);
        else onIconUpdate(-1);
    }

    @Override
    public String getTitle() {
        return getContext().getString(R.string.icon_bluetooth);
    }

    @Override
    public String[] getPermissions() {
        return new String[]{Manifest.permission.BLUETOOTH};
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
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_round),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_bluetooth_round"
                        ),
                        IconStyleData.fromResource(
                                getContext().getString(R.string.icon_style_outline),
                                IconStyleData.TYPE_VECTOR,
                                getContext(),
                                "ic_bluetooth_outline"
                        )
                )
        );

        styles.removeAll(Collections.singleton(null));
        return styles;
    }

    @Override
    public String[] getIconNames() {
        return new String[]{
                getContext().getString(R.string.icon_bluetooth_scanning),
                getContext().getString(R.string.icon_bluetooth_connected)
        };
    }

    static class BluetoothReceiver extends IconUpdateReceiver<BluetoothIconData> {

        private BluetoothReceiver(BluetoothIconData iconData) {
            super(iconData);
        }

        @Override
        public void onReceive(BluetoothIconData icon, Intent intent) {
            int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.STATE_OFF);
            if (state != BluetoothAdapter.STATE_OFF)
                icon.onIconUpdate(state == BluetoothAdapter.STATE_CONNECTED ? 1 : 0);
            else icon.onIconUpdate(-1);
        }
    }
}
