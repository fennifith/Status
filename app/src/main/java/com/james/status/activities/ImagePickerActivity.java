package com.james.status.activities;

import android.content.Intent;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.FrameLayout;

import com.james.status.Status;

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
    protected void onDestroy() {
        status.onActivityResult(0, RESULT_CANCELED, null);
        super.onDestroy();
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        status.onActivityResult(requestCode, resultCode, data);
        finish();
    }
}
