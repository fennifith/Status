package com.james.status;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.support.annotation.Nullable;
import android.util.Log;
import android.widget.Toast;

import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;

import java.util.ArrayList;
import java.util.List;

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
