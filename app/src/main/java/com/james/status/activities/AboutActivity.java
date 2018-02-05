package com.james.status.activities;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.HapticFeedbackConstants;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageView;

import com.bumptech.glide.Glide;
import com.james.status.R;
import com.james.status.dialogs.LicenseDialog;
import com.james.status.utils.UrlClickListener;


public class AboutActivity extends AppCompatActivity {

    private ImageView jamesImage;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_about);

        setSupportActionBar((Toolbar) findViewById(R.id.toolbar));
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        jamesImage = (ImageView) findViewById(R.id.jamesImage);
        Glide.with(this).load("https://jfenn.me/images/headers/rocks.jpg").into(jamesImage);
        findViewById(R.id.james).setOnLongClickListener(new View.OnLongClickListener() {
            @Override
            public boolean onLongClick(View v) {
                v.performHapticFeedback(HapticFeedbackConstants.LONG_PRESS);
                Glide.with(AboutActivity.this).load("https://jfenn.me/images/headers/cabbage.jpg").into(jamesImage);
                return false;
            }
        });

        findViewById(R.id.jamesWebsite).setOnClickListener(new UrlClickListener("https://jfenn.me/"));

        findViewById(R.id.jamesGplus).setOnClickListener(new UrlClickListener("https://plus.google.com/114612442558529845038"));

        findViewById(R.id.jamesTwitter).setOnClickListener(new UrlClickListener("https://twitter.com/IDontLikePHP"));

        Glide.with(this).load("https://jfenn.me/images/headers/highway.jpg").into((ImageView) findViewById(R.id.anasImage));

        findViewById(R.id.anasGplus).setOnClickListener(new UrlClickListener("https://plus.google.com/101325935187431392674"));

        findViewById(R.id.anasTwitter).setOnClickListener(new UrlClickListener("https://twitter.com/MAKTHG"));

        Glide.with(this).load("https://jfenn.me/images/headers/vukheader.jpg").into((ImageView) findViewById(R.id.vukImage));

        findViewById(R.id.vukGplus).setOnClickListener(new UrlClickListener("https://plus.google.com/+Vuka%C5%A1inAn%C4%91elkovi%C4%87zavukodlak"));

        findViewById(R.id.vukPlay).setOnClickListener(new UrlClickListener("https://play.google.com/store/apps/dev?id=6941105890231522296"));

        findViewById(R.id.vukDribbble).setOnClickListener(new UrlClickListener("https://dribbble.com/zavukodlak"));

        Glide.with(this).load("https://jfenn.me/images/headers/onamountain.jpeg").into((ImageView) findViewById(R.id.ghostImage));

        findViewById(R.id.ghostWebsite).setOnClickListener(new UrlClickListener("https://technologx.com/"));

        findViewById(R.id.ghostGplus).setOnClickListener(new UrlClickListener("https://plus.google.com/109414818005290076221"));

        findViewById(R.id.ghostGithub).setOnClickListener(new UrlClickListener("https://github.com/Technologx"));

        findViewById(R.id.ryo).setOnClickListener(new UrlClickListener("https://github.com/Ryo567"));

        findViewById(R.id.opnay).setOnClickListener(new UrlClickListener("https://github.com/opnay"));

        findViewById(R.id.alsidi).setOnClickListener(new UrlClickListener("https://github.com/Alsidi-Group"));

        findViewById(R.id.google).setOnClickListener(new UrlClickListener("https://material.io/icons/"));

        findViewById(R.id.md).setOnClickListener(new UrlClickListener("https://github.com/Templarian/MaterialDesign"));

        findViewById(R.id.icons8).setOnClickListener(new UrlClickListener("https://icons8.com/"));

        findViewById(R.id.libraries).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                new LicenseDialog(AboutActivity.this).show();
            }
        });

        findViewById(R.id.policy).setOnClickListener(new UrlClickListener("https://jfenn.me/policies/?status"));
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
