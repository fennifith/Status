package com.james.status.utils.tasks;

import android.content.Context;
import android.content.pm.ApplicationInfo;
import android.content.pm.PackageManager;
import android.os.AsyncTask;

import com.james.status.data.AppPreferenceData;

import java.lang.ref.WeakReference;
import java.util.List;

public class PackagesGetterTask extends AsyncTask<Object, Integer, AppPreferenceData[]> {

    private WeakReference<Context> context;
    private WeakReference<OnGottenListener> listener;

    public PackagesGetterTask(Context context, OnGottenListener listener) {
        this.context = new WeakReference<>(context);
        this.listener = new WeakReference<>(listener);
    }

    @Override
    protected AppPreferenceData[] doInBackground(Object... objects) {
        Context context = this.context.get();
        if (context != null) {
            PackageManager manager = context.getPackageManager();
            if (manager != null) {
                try {
                    Thread.sleep(100);
                    List<ApplicationInfo> packages = manager.getInstalledApplications(0);
                    publishProgress(1, packages.size());
                    AppPreferenceData[] preferences = new AppPreferenceData[packages.size()];
                    Thread.sleep(100);
                    for (int i = 0; i < packages.size(); i++) {
                        preferences[i] = new AppPreferenceData(context, packages.get(i).packageName);
                        publishProgress(i + 1, (int) (packages.size() * 1.3));
                        Thread.sleep(5);
                    }

                    publishProgress(1, 1);
                    return preferences;
                } catch (InterruptedException ignored) {
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
