package com.james.status.utils.tasks;

import android.content.Context;
import android.content.pm.ActivityInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.james.status.data.AppPreferenceData;

import java.lang.ref.WeakReference;

public class ActivitiesGetterTask extends AsyncTask<String, Integer, AppPreferenceData[]> {

    private WeakReference<Context> context;
    private WeakReference<OnGottenListener> listener;

    public ActivitiesGetterTask(Context context, OnGottenListener listener) {
        this.context = new WeakReference<>(context);
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected AppPreferenceData[] doInBackground(String... objects) {
        Context context = this.context.get();
        if (context != null) {
            PackageManager manager = context.getPackageManager();
            if (manager != null) {
                try {
                    Thread.sleep(100);
                    ActivityInfo[] activities = manager.getPackageInfo(objects[0], PackageManager.GET_ACTIVITIES).activities;
                    if (activities != null) {
                        publishProgress(1, activities.length);
                        AppPreferenceData[] preferences = new AppPreferenceData[activities.length];
                        Thread.sleep(100);
                        for (int i = 0; i < activities.length; i++) {
                            preferences[i] = new AppPreferenceData(context, objects[0] + "/" + activities[i].name);
                            publishProgress(i + 1, (int) (activities.length * 1.3));
                            Thread.sleep(5);
                        }

                        publishProgress(1, 1);
                        return preferences;
                    }
                } catch (InterruptedException | PackageManager.NameNotFoundException ignored) {
                }
            }
        }

        return new AppPreferenceData[0];
    }

    @Override
    protected void onProgressUpdate(Integer... values) {
        super.onProgressUpdate(values);
        OnGottenListener listener = this.listener.get();
        if (listener != null)
            listener.onGottenProgressUpdate(values[0], values[1]);
    }

    @Override
    protected void onPostExecute(AppPreferenceData[] strings) {
        super.onPostExecute(strings);
        OnGottenListener listener = this.listener.get();
        if (listener != null)
            listener.onGottenPackages(strings);
    }

    public interface OnGottenListener {
        void onGottenProgressUpdate(int progress, int max);

        void onGottenPackages(AppPreferenceData[] packages);
    }
}
