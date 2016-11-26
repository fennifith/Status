package com.james.status;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.james.status.utils.PreferenceUtils;

import java.util.ArrayList;
import java.util.List;

public class Status extends Application {

    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnColorPickedListener> onColorPickedListeners;
    private List<OnPreferenceChangedListener> onPreferenceChangedListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onColorPickedListeners = new ArrayList<>();
        onPreferenceChangedListeners = new ArrayList<>();
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void addListener(OnColorPickedListener listener) {
        onColorPickedListeners.add(listener);
    }

    public void addListener(OnPreferenceChangedListener listener) {
        onPreferenceChangedListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
    }

    public void removeListener(OnColorPickedListener listener) {
        onColorPickedListeners.remove(listener);
    }

    public void removeListener(OnPreferenceChangedListener listener) {
        onPreferenceChangedListeners.remove(listener);
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

    public void onPreferenceChanged() {
        for (OnPreferenceChangedListener listener : onPreferenceChangedListeners) {
            listener.onPreferenceChanged();
        }
    }

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnColorPickedListener {
        void onColorPicked(@Nullable Integer color);
    }

    public interface OnPreferenceChangedListener {
        void onPreferenceChanged();
    }

    public static void showDebug(Context context, String message, int length) {
        if (isDebug(context))
            Toast.makeText(context, message, length).show();
        else Log.d("Status", message);
    }

    public static boolean isDebug(Context context) {
        Boolean isDebug = PreferenceUtils.getBooleanPreference(context, PreferenceUtils.PreferenceIdentifier.STATUS_DEBUG);
        return (isDebug != null && isDebug) || (isDebug == null && BuildConfig.DEBUG);
    }
}
