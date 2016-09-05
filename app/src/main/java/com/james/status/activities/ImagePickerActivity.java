package com.james.status.activities;

import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.james.status.Status;
import com.james.status.dialogs.ImageColorPickerDialog;
import com.james.status.dialogs.PreferenceDialog;

import java.io.IOException;

public class ImagePickerActivity extends AppCompatActivity {

    public static int ACTION_PICK_IMAGE = 1432;

    private Status status;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(new FrameLayout(this));

        status = (Status) getApplicationContext();

        Intent intent = new Intent(Intent.ACTION_PICK);
        intent.setType("image/*");
        startActivityForResult(intent, ACTION_PICK_IMAGE);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        status.onActivityResult(requestCode, resultCode, data);

        Bitmap bitmap = null;

        if (requestCode == ACTION_PICK_IMAGE) {
            if (resultCode == RESULT_OK) {
                try {
                    bitmap = MediaStore.Images.Media.getBitmap(getContentResolver(), data.getData());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        if (bitmap != null) {
            new ImageColorPickerDialog(this, bitmap).setDefaultPreference(Color.BLACK).setListener(new PreferenceDialog.OnPreferenceListener<Integer>() {
                @Override
                public void onPreference(PreferenceDialog dialog, Integer preference) {
                    status.onColor(preference);
                    finish();
                }

                @Override
                public void onCancel(PreferenceDialog dialog) {
                    status.onColor(null);
                    finish();
                }
            }).show();
        } else {
            status.onColor(null);
            finish();
        }
    }
}
