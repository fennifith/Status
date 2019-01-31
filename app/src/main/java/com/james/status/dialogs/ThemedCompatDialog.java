package com.james.status.dialogs;

import android.content.Context;

import com.james.status.R;
import com.james.status.data.PreferenceData;

import androidx.annotation.StyleRes;
import androidx.appcompat.app.AppCompatDialog;

public abstract class ThemedCompatDialog extends AppCompatDialog {

    public enum Type {
        NORMAL(R.style.AppTheme_Dialog, R.style.AppTheme_Dark_Dialog),
        FULLSCREEN(R.style.AppTheme_Dialog_FullScreen, R.style.AppTheme_Dark_Dialog_FullScreen),
        BOTTOM_SHEET(R.style.AppTheme_Dialog_BottomSheet, R.style.AppTheme_Dark_Dialog_BottomSheet);

        private int lightTheme, darkTheme;

        Type(@StyleRes int lightTheme, @StyleRes int darkTheme) {
            this.lightTheme = lightTheme;
            this.darkTheme = darkTheme;
        }

        @StyleRes
        int getTheme(Context context) {
            return PreferenceData.PREF_DARK_THEME.getValue(context) ? darkTheme : lightTheme;
        }
    }

    public ThemedCompatDialog(Context context) {
        this(context, Type.NORMAL);
    }

    public ThemedCompatDialog(Context context, Type type) {
        super(context, type.getTheme(context));
    }

}
