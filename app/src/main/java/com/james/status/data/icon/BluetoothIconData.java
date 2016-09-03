package com.james.status.data.icon;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.graphics.drawable.VectorDrawableCompat;

import com.james.status.R;
import com.james.status.data.IconStyleData;
import com.james.status.utils.StaticUtils;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class BluetoothIconData extends IconData<BluetoothIconData.BluetoothReceiver> {

    public BluetoothIconData(Context context) {
        super(context);
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
    public String getTitle() {
        return getContext().getString(R.string.icon_bluetooth);
    }

    @Override
    public List<IconStyleData> getIconStyles() {
        List<IconStyleData> styles = new ArrayList<>();

        styles.addAll(
                Arrays.asList(
                        new IconStyleData(
                                getContext().getString(R.string.icon_style_default),
                                R.drawable.ic_bluetooth,
                                R.drawable.ic_bluetooth_connected
                        )
                )
        );

        return styles;
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
