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

import com.james.status.data.PreferenceData;
import com.james.status.utils.StaticUtils;

public class ActivityFullScreenSettingReceiver extends BroadcastReceiver {

    public static final String EXTRA_COMPONENT = "com.james.status.EXTRA_COMPONENT";
    public static final String EXTRA_FULLSCREEN = "com.james.status.EXTRA_FULLSCREEN";

    @Override
    public void onReceive(Context context, Intent intent) {
        if (intent.hasExtra(EXTRA_COMPONENT) && intent.hasExtra(EXTRA_FULLSCREEN)) {
            PreferenceData.APP_FULLSCREEN.setValue(context, !intent.getBooleanExtra(EXTRA_FULLSCREEN, true), intent.getStringExtra(EXTRA_COMPONENT));
            StaticUtils.updateStatusService(context, true);
        }
    }
}
