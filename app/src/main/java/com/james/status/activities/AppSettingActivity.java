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

import com.james.status.data.AppPreferenceData;
import com.james.status.dialogs.preference.AppPreferenceDialog;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

public class AppSettingActivity extends AppCompatActivity {

    public final static String EXTRA_COMPONENT = "com.james.status.EXTRA_COMPONENT";

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        AppPreferenceDialog dialog = new AppPreferenceDialog(this, new AppPreferenceData(this, getIntent().getStringExtra(EXTRA_COMPONENT)));
        dialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
            @Override
            public void onDismiss(DialogInterface dialog) {
                finish();
            }
        });
        dialog.show();
    }
}
