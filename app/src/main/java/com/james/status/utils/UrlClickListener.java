package com.james.status.utils;

import android.content.Intent;
import android.net.Uri;
import android.view.View;

public class UrlClickListener implements View.OnClickListener {

    private String url;

    public UrlClickListener(String url) {
        this.url = url;
    }

    @Override
    public void onClick(View v) {
        v.getContext().startActivity(new Intent(Intent.ACTION_VIEW, Uri.parse(url)));
    }
}
