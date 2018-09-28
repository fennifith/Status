package com.james.status.utils.tasks;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.AsyncTask;
import android.preference.PreferenceManager;

import com.james.status.data.PreferenceData;

import java.lang.ref.WeakReference;
import java.util.Map;

public class PreferenceUpdateTask extends AsyncTask<Object, Object, Object> {

    private WeakReference<Context> context;

    public PreferenceUpdateTask(Context context) {
        this.context = new WeakReference<>(context);
    }

    @Override
    protected Object doInBackground(Object... objects) {
        Context context = this.context.get();
        if (context == null)
            return null;

        SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
        Map<String, ?> items = prefs.getAll();
        for (String key : items.keySet()) {
            if ((int) PreferenceData.PREF_VERSION.getValue(context) == 0) {
                if (key.startsWith("COLOR/")) {
                    String[] packages = key.substring("COLOR/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("COLOR/" + packages[0] + "/" + packages[1])) {
                        try {
                            PreferenceData.APP_COLOR.setValue(context, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (key.startsWith("CACHE_COLOR/")) {
                    String[] packages = key.substring("CACHE_COLOR/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("CACHE_COLOR/" + packages[0] + "/" + packages[1])) {
                        try {
                            PreferenceData.APP_COLOR_CACHE.setValue(context, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (key.startsWith("CACHE_VERSION/")) {
                    String[] packages = key.substring("CACHE_VERSION/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("CACHE_VERSION/" + packages[0] + "/" + packages[1])) {
                        try {
                            PreferenceData.APP_COLOR_CACHE_VERSION.setValue(context, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (key.startsWith("FULLSCREEN/")) {
                    String[] packages = key.substring("FULLSCREEN/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("FULLSCREEN/" + packages[0] + "/" + packages[1])) {
                        try {
                            if (prefs.getBoolean(key, false))
                                PreferenceData.APP_FULLSCREEN.setValue(context, true, packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (key.startsWith("IGNORE_AUTO_DETECT/")) {
                    String[] packages = key.substring("IGNORE_AUTO_DETECT/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("IGNORE_AUTO_DETECT/" + packages[0] + "/" + packages[1])) {
                        try {
                            if (prefs.getBoolean(key, false))
                                PreferenceData.APP_FULLSCREEN.setValue(context, true, packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else if (key.startsWith("NOTIFICATIONS/")) {
                    String[] packages = key.substring("NOTIFICATIONS/".length()).split("/");
                    if (packages.length == 2 && prefs.contains("NOTIFICATIONS/" + packages[0] + "/" + packages[1])) {
                        try {
                            if (!prefs.getBoolean(key, true))
                                PreferenceData.APP_NOTIFICATIONS.setValue(context, false, packages[0] + "/" + packages[1]);
                        } catch (Exception ignored) {
                        }
                    }
                } else continue;
            }

            prefs.edit().remove(key).apply();

            try {
                Thread.sleep(20);
            } catch (InterruptedException e) {
                return null;
            }
        }

        PreferenceData.PREF_VERSION.setValue(context, PreferenceData.VERSION);

        return null;
    }

}
