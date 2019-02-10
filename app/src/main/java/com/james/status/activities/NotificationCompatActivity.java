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

package com.james.status.activities;

import android.content.DialogInterface;
import android.os.Bundle;
import android.widget.FrameLayout;

import com.james.status.dialogs.CompatibilityNotificationDialog;
import com.james.status.services.StatusServiceImpl;
import com.james.status.utils.StaticUtils;

import androidx.appcompat.app.AppCompatActivity;

public class NotificationCompatActivity extends AppCompatActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FrameLayout(this));
        StatusServiceImpl.stop(this);

        CompatibilityNotificationDialog dialog = new CompatibilityNotificationDialog(this);
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                StatusServiceImpl.stop(NotificationCompatActivity.this);
                if (StaticUtils.shouldUseCompatNotifications(NotificationCompatActivity.this)) {
                    setResult(RESULT_OK);
                    finish();
                } else {
                    setResult(RESULT_CANCELED);
                    finish();
                }
            }
        });
        dialog.show();
    }
}
