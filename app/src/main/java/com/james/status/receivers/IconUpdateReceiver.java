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
