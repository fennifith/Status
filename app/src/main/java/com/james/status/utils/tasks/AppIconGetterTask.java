package com.james.status.utils.tasks;

import android.content.pm.PackageManager;
import android.graphics.Bitmap;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;

import com.james.status.data.AppPreferenceData;

public class AppIconGetterTask extends AsyncTask<AppPreferenceData, Integer, Drawable> {

    private PackageManager manager;
    private OnGottenListener listener;

    public AppIconGetterTask(PackageManager manager, OnGottenListener listener) {
        this.manager = manager;
        this.listener = listener;
    }

    @Override
    protected Drawable doInBackground(AppPreferenceData... appPreferenceData) {
        try {
            return manager.getApplicationIcon(appPreferenceData[0].getPackageName());
        } catch (PackageManager.NameNotFoundException e) {
            return null;
        }
    }

    @Override
    protected void onPostExecute(Drawable drawable) {
        super.onPostExecute(drawable);
        if (listener != null)
            listener.onGotten(drawable);
    }

    public interface OnGottenListener {
        void onGotten(Drawable drawable);
    }
}
