package com.james.status;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.preference.PreferenceManager;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Status extends Application {

    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnColorPickedListener> onColorPickedListeners;
    private List<OnIconPreferenceChangedListener> onIconPreferenceChangedListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onColorPickedListeners = new ArrayList<>();
        onIconPreferenceChangedListeners = new ArrayList<>();

        if ((int) PreferenceData.PREF_VERSION.getValue(this) == 0) {
            new Thread() {
                @Override
                public void run() {
                    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(Status.this);
                    Map<String, ?> items = prefs.getAll();
                    SharedPreferences.Editor editor = prefs.edit();
                    for (String key : items.keySet()) {
                        if (key.startsWith("COLOR/")) {
                            String[] packages = key.substring("COLOR/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("COLOR/" + packages[0] + "/" + packages[1])) {
                                try {
                                    PreferenceData.APP_COLOR.setValue(Status.this, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (key.startsWith("CACHE_COLOR/")) {
                            String[] packages = key.substring("CACHE_COLOR/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("CACHE_COLOR/" + packages[0] + "/" + packages[1])) {
                                try {
                                    PreferenceData.APP_COLOR_CACHE.setValue(Status.this, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (key.startsWith("CACHE_VERSION/")) {
                            String[] packages = key.substring("CACHE_VERSION/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("CACHE_VERSION/" + packages[0] + "/" + packages[1])) {
                                try {
                                    PreferenceData.APP_COLOR_CACHE_VERSION.setValue(Status.this, prefs.getInt(key, 0), packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (key.startsWith("FULLSCREEN/")) {
                            String[] packages = key.substring("FULLSCREEN/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("FULLSCREEN/" + packages[0] + "/" + packages[1])) {
                                try {
                                    if (prefs.getBoolean(key, false))
                                        PreferenceData.APP_FULLSCREEN.setValue(Status.this, true, packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (key.startsWith("IGNORE_AUTO_DETECT/")) {
                            String[] packages = key.substring("IGNORE_AUTO_DETECT/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("IGNORE_AUTO_DETECT/" + packages[0] + "/" + packages[1])) {
                                try {
                                    if (prefs.getBoolean(key, false))
                                        PreferenceData.APP_FULLSCREEN.setValue(Status.this, true, packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else if (key.startsWith("NOTIFICATIONS/")) {
                            String[] packages = key.substring("NOTIFICATIONS/".length()).split("/");
                            if (packages.length == 2 && prefs.contains("NOTIFICATIONS/" + packages[0] + "/" + packages[1])) {
                                try {
                                    if (!prefs.getBoolean(key, true))
                                        PreferenceData.APP_NOTIFICATIONS.setValue(Status.this, false, packages[0] + "/" + packages[1]);
                                } catch (Exception ignored) {
                                }
                            }
                        } else continue;

                        editor.remove(key);
                    }
                    editor.putInt("PREF_VERSION", PreferenceData.VERSION);
                    editor.apply();
                }
            }.start();
        }
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void addListener(OnColorPickedListener listener) {
        onColorPickedListeners.add(listener);
    }

    public void addListener(OnIconPreferenceChangedListener listener) {
        onIconPreferenceChangedListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
    }

    public void removeListener(OnColorPickedListener listener) {
        onColorPickedListeners.remove(listener);
    }

    public void removeListener(OnIconPreferenceChangedListener listener) {
        onIconPreferenceChangedListeners.remove(listener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : onActivityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
        }
    }

    public void onColor(@Nullable Integer color) {
        for (OnColorPickedListener listener : onColorPickedListeners) {
            listener.onColorPicked(color);
        }
    }

    public void onIconPreferenceChanged(IconData... icons) {
        for (OnIconPreferenceChangedListener listener : onIconPreferenceChangedListeners) {
            listener.onIconPreferenceChanged(icons);
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnColorPickedListener {
        void onColorPicked(@Nullable Integer color);
    }

    public interface OnIconPreferenceChangedListener {
        void onIconPreferenceChanged(IconData... icons);
    }

    public static void showDebug(Context context, String message, int length) {
        if (PreferenceData.STATUS_DEBUG.getValue(context))
            Toast.makeText(context, message, length).show();
        else Log.d("Status", message);
    }
}
