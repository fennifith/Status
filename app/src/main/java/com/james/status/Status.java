package com.james.status;

import android.app.Application;
import android.content.Intent;
import android.support.annotation.Nullable;

import java.util.ArrayList;
import java.util.List;

public class Status extends Application {

    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnColorPickedListener> onColorPickedListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onColorPickedListeners = new ArrayList<>();
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void addListener(OnColorPickedListener listener) {
        onColorPickedListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
    }

    public void removeListener(OnColorPickedListener listener) {
        onColorPickedListeners.remove(listener);
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

    public interface OnActivityResultListener {
        void onActivityResult(int requestCode, int resultCode, Intent data);
    }

    public interface OnColorPickedListener {
        void onColorPicked(@Nullable Integer color);
    }
}
