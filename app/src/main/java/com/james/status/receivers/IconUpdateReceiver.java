package com.james.status.receivers;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;

import com.james.status.data.icon.IconData;

import java.lang.ref.SoftReference;

public abstract class IconUpdateReceiver<T extends IconData> extends BroadcastReceiver {

    private SoftReference<T> reference;

    public IconUpdateReceiver(T iconData) {
        reference = new SoftReference<>(iconData);
    }

    @Override
    public final void onReceive(Context context, Intent intent) {
        T icon = reference.get();
        if (icon != null) onReceive(icon, intent);
    }

    public abstract void onReceive(T icon, Intent intent);
}
