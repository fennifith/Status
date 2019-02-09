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

import android.Manifest;
import android.content.Intent;
import android.os.Bundle;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.james.status.R;
import com.james.status.Status;
import com.james.status.utils.StaticUtils;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class ImagePickerActivity extends AppCompatActivity {

    public static int ACTION_PICK_IMAGE = 1432;

    private Status status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FrameLayout(this));

        status = (Status) getApplicationContext();

        String[] permissions = new String[]{Manifest.permission.READ_EXTERNAL_STORAGE, Manifest.permission.WRITE_EXTERNAL_STORAGE};
        if (!StaticUtils.isPermissionsGranted(this, permissions)) {
            StaticUtils.requestPermissions(this, permissions);
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, ACTION_PICK_IMAGE);
        }
    }

    @Override
    protected void onDestroy() {
        status.onActivityResult(0, RESULT_CANCELED, null);
        super.onDestroy();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);

        if (!StaticUtils.isPermissionsGranted(this, permissions)) {
            Toast.makeText(this, R.string.msg_missing_storage_permission, Toast.LENGTH_SHORT).show();
            finish();
        } else {
            Intent intent = new Intent(Intent.ACTION_PICK);
            intent.setType("image/*");
            startActivityForResult(intent, ACTION_PICK_IMAGE);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        status.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
