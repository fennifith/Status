package com.james.status.activities;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;

import com.james.status.R;
import com.james.status.utils.PreferenceUtils;

public class SplashActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_splash);

        Boolean isTutorial = PreferenceUtils.getBooleanPreference(this, PreferenceUtils.PreferenceIdentifier.SHOW_TUTORIAL);

        if (isTutorial != null && !isTutorial) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    startActivity(new Intent(SplashActivity.this, MainActivity.class));
                    finish();
                }
            }, 2500);
        } else {
            startActivity(new Intent(this, IntroActivity.class));
            finish();
        }
    }
}
