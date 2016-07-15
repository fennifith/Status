package com.james.status.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.james.status.R;
import com.mikepenz.aboutlibraries.Libs;
import com.mikepenz.aboutlibraries.LibsBuilder;

public class AboutActivity extends AppCompatActivity {

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setHomeAsUpIndicator(R.drawable.ic_back);

        Glide.with(this).load("https://theandroidmaster.github.io/images/headers/status_bg.png").into((ImageView) findViewById(R.id.header));

        Glide.with(this).load("https://theandroidmaster.github.io/images/headers/rocks.jpg").into((ImageView) findViewById(R.id.jamesImage));

        findViewById(R.id.jamesWebsite).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://theandroidmaster.github.io/")));
            }
        });

        findViewById(R.id.jamesGplus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/114612442558529845038")));
            }
        });

        findViewById(R.id.jamesTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/IDontLikePHP")));
            }
        });

        Glide.with(this).load("https://theandroidmaster.github.io/images/headers/highway.jpg").into((ImageView) findViewById(R.id.anasImage));

        findViewById(R.id.anasGplus).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://plus.google.com/101325935187431392674")));
            }
        });

        findViewById(R.id.anasTwitter).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://twitter.com/MAKTHG")));
            }
        });

        findViewById(R.id.libraries).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LibsBuilder().withActivityStyle(Libs.ActivityStyle.LIGHT_DARK_TOOLBAR).start(AboutActivity.this);
            }
        });
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.menu_about, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                onBackPressed();
                break;
            case R.id.action_github:
                startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse("https://github.com/TheAndroidMaster/Status")));
                break;
        }
        return super.onOptionsItemSelected(item);
    }
}
