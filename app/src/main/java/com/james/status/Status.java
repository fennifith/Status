package com.james.status;

import android.app.Application;
import android.content.Context;
import android.content.Intent;
import android.util.Log;

import com.james.status.data.PreferenceData;
import com.james.status.data.icon.IconData;
import com.james.status.utils.tasks.PreferenceUpdateTask;

import java.util.ArrayList;
import java.util.List;

import androidx.annotation.Nullable;
import androidx.annotation.StyleRes;

public class Status extends Application {

    public enum Theme {
        ACTIVITY_NORMAL(R.style.AppTheme, R.style.AppTheme_Dark),
        ACTIVITY_SPLASH(R.style.AppTheme_Splash, R.style.AppTheme_Dark_Splash),
        DIALOG_NORMAL(R.style.AppTheme_Dialog, R.style.AppTheme_Dark_Dialog),
        DIALOG_FULL_SCREEN(R.style.AppTheme_Dialog_FullScreen, R.style.AppTheme_Dark_Dialog_FullScreen),
        DIALOG_BOTTOM_SHEET(R.style.AppTheme_Dialog_BottomSheet, R.style.AppTheme_Dark_Dialog_BottomSheet);

        private int lightTheme, darkTheme;

        Theme(@StyleRes int lightTheme, @StyleRes int darkTheme) {
            this.lightTheme = lightTheme;
            this.darkTheme = darkTheme;
        }

        @StyleRes
        public int getTheme(Context context) {
            return PreferenceData.PREF_DARK_THEME.getValue(context) ? darkTheme : lightTheme;
        }
    }

    private List<OnActivityResultListener> onActivityResultListeners;
    private List<OnIconPreferenceChangedListener> onIconPreferenceChangedListeners;

    @Override
    public void onCreate() {
        super.onCreate();

        onActivityResultListeners = new ArrayList<>();
        onIconPreferenceChangedListeners = new ArrayList<>();

        if (PreferenceData.VERSION != (Integer) PreferenceData.PREF_VERSION.getValue(this))
            new PreferenceUpdateTask(this).execute();
    }

    public void addListener(OnActivityResultListener listener) {
        onActivityResultListeners.add(listener);
    }

    public void addListener(OnIconPreferenceChangedListener listener) {
        onIconPreferenceChangedListeners.add(listener);
    }

    public void removeListener(OnActivityResultListener listener) {
        onActivityResultListeners.remove(listener);
    }

    public void removeListener(OnIconPreferenceChangedListener listener) {
        onIconPreferenceChangedListeners.remove(listener);
    }

    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        for (OnActivityResultListener listener : onActivityResultListeners) {
            listener.onActivityResult(requestCode, resultCode, data);
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
        if (BuildConfig.DEBUG)
            Log.d("Status", message);
    }
}
